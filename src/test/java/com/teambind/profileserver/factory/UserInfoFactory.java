package com.teambind.profileserver.factory;

import com.teambind.profileserver.entity.UserGenres;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.UserInstruments;
import com.teambind.profileserver.entity.key.UserGenreKey;
import com.teambind.profileserver.entity.key.UserInstrumentKey;
import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import com.teambind.profileserver.enums.City;
import com.teambind.profileserver.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 대량 테스트 데이터를 생성하는 팩토리. (테스트 전용)
 * - userId: user_000001 형식
 * - nickname: nick_000001 형식 (UNIQUE 제약 충족)
 * - 장르/악기 매핑 포함
 */
@Component
public class UserInfoFactory {

    private static final Logger log = LoggerFactory.getLogger(UserInfoFactory.class);

    private final UserInfoRepository userInfoRepository;
    private final UserGenresRepository userGenresRepository;
    private final UserInstrumentsRepository userInstrumentsRepository;
    private final GenreNameTableRepository genreNameTableRepository;
    private final InstrumentNameTableRepository instrumentNameTableRepository;
    private final EntityManager em;

    @Autowired
    public UserInfoFactory(UserInfoRepository userInfoRepository,
                           UserGenresRepository userGenresRepository,
                           UserInstrumentsRepository userInstrumentsRepository,
                           GenreNameTableRepository genreNameTableRepository,
                           InstrumentNameTableRepository instrumentNameTableRepository,
                           EntityManager em) {
        this.userInfoRepository = userInfoRepository;
        this.userGenresRepository = userGenresRepository;
        this.userInstrumentsRepository = userInstrumentsRepository;
        this.genreNameTableRepository = genreNameTableRepository;
        this.instrumentNameTableRepository = instrumentNameTableRepository;
        this.em = em;
    }

    /**
     * 장르/악기 명칭 테이블이 비어있다면 기본 데이터로 시드합니다. (FK 제약 대응)
     */
    @Transactional
    public void ensureNameTablesSeeded(int genreCount, int instrumentCount) {
        if (genreNameTableRepository.count() == 0) {
            List<GenreNameTable> genres = new ArrayList<>();
            for (int i = 1; i <= genreCount; i++) {
                genres.add(GenreNameTable.builder()
                        .genreId(i)
                        .genreName("GENRE_" + i)
                        .build());
            }
            genreNameTableRepository.saveAll(genres);
            log.info("Seeded {} genres", genreCount);
        }
        if (instrumentNameTableRepository.count() == 0) {
            List<InstrumentNameTable> instruments = new ArrayList<>();
            for (int i = 1; i <= instrumentCount; i++) {
                instruments.add(InstrumentNameTable.builder()
                        .instrumentId(i)
                        .instrumentName("INSTRUMENT_" + i)
                        .build());
            }
            instrumentNameTableRepository.saveAll(instruments);
            log.info("Seeded {} instruments", instrumentCount);
        }
    }

    /**
     * 약 10만 명의 유저와 연관 정보(장르/악기)를 생성합니다.
     */
    @Transactional
    public void generate100kUsers() {
        generateUsers(100_000, 1000);
    }

    /**
     * 대량 유저 생성 (배치 저장)
     * @param total 생성할 총 유저 수
     * @param batchSize 배치 크기 (예: 1000)
     */
    @Transactional
    public void generateUsers(int total, int batchSize) {
        generateUsersRange(1, total, batchSize);
    }

    /**
     * 이미 생성된 데이터가 있으면 건너뛰고, 부족한 만큼만 이어서 생성합니다.
     * 예: 기존에 user_000001 ~ user_020000 까지 있으면, 나머지 80,001 ~ 100,000만 생성
     */
    @Transactional
    public void ensureUsersGenerated(int total, int batchSize) {
        long existing = userInfoRepository.countByUserIdStartingWith("user_");
        if (existing >= total) {
            log.info("Skip generation. Already have {} users (>= {}).", existing, total);
            return;
        }
        int start = (int) existing + 1;
        log.info("Generating missing users: {} ~ {}", start, total);
        generateUsersRange(start, total, batchSize);
    }

    /**
     * 지정한 범위의 user 번호로 데이터를 생성합니다. (start <= N <= end)
     */
    @Transactional
    public void generateUsersRange(int start, int end, int batchSize) {
        if (start > end) return;
        // FK 테이블이 비어있다면 기본 시드 (장르 50, 악기 30개)
        ensureNameTablesSeeded(50, 30);

        // 현재 존재하는 장르/악기 ID 풀 확보
        List<Integer> genrePool = genreNameTableRepository.findAll()
                .stream().map(GenreNameTable::getGenreId).sorted().collect(Collectors.toList());
        List<Integer> instrumentPool = instrumentNameTableRepository.findAll()
                .stream().map(InstrumentNameTable::getInstrumentId).sorted().collect(Collectors.toList());

        Random rnd = new Random(42); // 재현 가능성
        LocalDateTime now = LocalDateTime.now();

        List<UserInfo> userBatch = new ArrayList<>(batchSize);
        List<UserGenres> ugBatch = new ArrayList<>(batchSize * 2);
        List<UserInstruments> uiBatch = new ArrayList<>(batchSize * 2);

        for (int i = start; i <= end; i++) {
            String id = String.format(Locale.ROOT, "user_%06d", i);
                String nickname = String.format(Locale.ROOT, "nick_%06d", i);

            City city = randomCity(rnd);
            Character sex = rnd.nextBoolean() ? 'M' : 'F';
            boolean isPublic = rnd.nextBoolean();
            boolean isChatable = rnd.nextBoolean();

            UserInfo user = UserInfo.builder()
                    .userId(id)
                    .nickname(nickname)
                    .city(city)
                    .sex(sex)
                    .isPublic(isPublic)
                    .isChatable(isChatable)
                    .createdAt(now.minusDays(rnd.nextInt(365)))
                    .updatedAt(now)
                    .profileImageUrl(null)
                    .build();
            userBatch.add(user);

            int genreCount = 1 + rnd.nextInt(3); // 1~3개
            int instrumentCount = 1 + rnd.nextInt(2); // 1~2개

            Set<Integer> pickedGenres = pickDistinct(genrePool, genreCount, rnd);
            for (Integer gid : pickedGenres) {
                UserGenreKey key = new UserGenreKey();
                setUserGenreKey(key, id, gid);
                UserGenres ug = UserGenres.builder()
                        .userId(key)
                        .userInfo(user)
                        .genre(em.getReference(GenreNameTable.class, gid))
                        .build();
                ugBatch.add(ug);
            }

            Set<Integer> pickedInstruments = pickDistinct(instrumentPool, instrumentCount, rnd);
            for (Integer insId : pickedInstruments) {
                UserInstrumentKey key = new UserInstrumentKey();
                setUserInstrumentKey(key, id, insId);
                UserInstruments ui = UserInstruments.builder()
                        .userId(key)
                        .userInfo(user)
                        .instrument(em.getReference(InstrumentNameTable.class, insId))
                        .build();
                uiBatch.add(ui);
            }

            if ((i - start + 1) % batchSize == 0) {
                flushBatches(userBatch, ugBatch, uiBatch);
            }
        }
        // 남은 데이터 처리
        flushBatches(userBatch, ugBatch, uiBatch);

        log.info("Generated users in range [{}..{}] with genres/instruments", start, end);
    }

    private void flushBatches(List<UserInfo> userBatch, List<UserGenres> ugBatch, List<UserInstruments> uiBatch) {
        if (!userBatch.isEmpty()) {
            userInfoRepository.saveAll(userBatch);
            userBatch.clear();
        }
        if (!ugBatch.isEmpty()) {
            userGenresRepository.saveAll(ugBatch);
            ugBatch.clear();
        }
        if (!uiBatch.isEmpty()) {
            userInstrumentsRepository.saveAll(uiBatch);
            uiBatch.clear();
        }
        // 영속성 컨텍스트를 비워 메모리 사용을 줄임
        em.flush();
        em.clear();
    }

    private static void setUserGenreKey(UserGenreKey key, String userId, int genreId) {
        try {
            var f1 = UserGenreKey.class.getDeclaredField("userId");
            var f2 = UserGenreKey.class.getDeclaredField("genreId");
            f1.setAccessible(true); f2.setAccessible(true);
            f1.set(key, userId); f2.set(key, genreId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setUserInstrumentKey(UserInstrumentKey key, String userId, int instrumentId) {
        try {
            var f1 = UserInstrumentKey.class.getDeclaredField("userId");
            var f2 = UserInstrumentKey.class.getDeclaredField("instrumentId");
            f1.setAccessible(true); f2.setAccessible(true);
            f1.set(key, userId); f2.set(key, instrumentId);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static City randomCity(Random rnd) {
        City[] values = City.values();
        return values[rnd.nextInt(values.length)];
    }

    private static Set<Integer> pickDistinct(List<Integer> pool, int k, Random rnd) {
        if (pool.isEmpty()) return Collections.emptySet();
        k = Math.min(k, pool.size());
        Set<Integer> res = new HashSet<>();
        while (res.size() < k) {
            res.add(pool.get(rnd.nextInt(pool.size())));
        }
        return res;
    }
}
