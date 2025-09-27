package com.teambind.profileserver.service.search;

import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.entity.UserGenres;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.UserInstruments;
import com.teambind.profileserver.entity.key.UserGenreKey;
import com.teambind.profileserver.entity.key.UserInstrumentKey;
import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import com.teambind.profileserver.repository.*;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 프로필 검색 서비스 통합 테스트
 * - 각 테스트에서 필요한 최소한의 데이터만 생성하여 IO 비용을 줄입니다.
 * - 생성, 조회, 검증 흐름을 한글 주석과 로그로 명확히 남깁니다.
 */
@SpringBootTest(classes = com.teambind.profileserver.ProfileServerApplication.class)
class ProfileSearchServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ProfileSearchServiceTest.class);

    @Autowired private ProfileSearchService profileSearchService;

    @Autowired private UserInfoRepository userInfoRepository;
    @Autowired private UserGenresRepository userGenresRepository;
    @Autowired private UserInstrumentsRepository userInstrumentsRepository;
    @Autowired private GenreNameTableRepository genreNameTableRepository;
    @Autowired private InstrumentNameTableRepository instrumentNameTableRepository;

    // 테스트 고립성을 위해 매번 정리
    @AfterEach
    void tearDown() {
        log.info("[정리] 테스트 데이터 정리 시작");
        userInstrumentsRepository.deleteAll();
        userGenresRepository.deleteAll();
        userInfoRepository.deleteAll();
        log.info("[정리] 테스트 데이터 정리 완료");
    }

    // 편의 메서드: 장르/악기 명칭 테이블에 필요한 식별자만 시드
    private void ensureNameTables(int[] genreIds, int[] instrumentIds) {
        for (int gid : genreIds) {
            if (!genreNameTableRepository.existsById(gid)) {
                genreNameTableRepository.save(GenreNameTable.builder().genreId(gid).genreName("GENRE_" + gid).build());
            }
        }
        for (int iid : instrumentIds) {
            if (!instrumentNameTableRepository.existsById(iid)) {
                instrumentNameTableRepository.save(InstrumentNameTable.builder().instrumentId(iid).instrumentName("INSTRUMENT_" + iid).build());
            }
        }
    }

    // 편의 메서드: 테스트용 유저 + 장르/악기 매핑 생성
    private UserInfo createUser(String id, String nickname, Character sex, Integer[] genres, Integer[] instruments) {
        log.info("[데이터 준비] 유저 생성: id={}, nickname={}, sex={}", id, nickname, sex);
        UserInfo user = UserInfo.builder()
                .userId(id)
                .nickname(nickname)
                .sex(sex)
                .build();
        user = userInfoRepository.save(user);

        if (genres != null) {
            for (Integer gid : genres) {
                UserGenreKey key = new UserGenreKey();
                setUserGenreKey(key, id, gid);
                UserGenres ug = UserGenres.builder()
                        .userId(key)
                        .userInfo(user)
                        .genre(GenreNameTable.builder().genreId(gid).build()) // reference만 필요
                        .build();
                userGenresRepository.save(ug);
            }
        }
        if (instruments != null) {
            for (Integer iid : instruments) {
                UserInstrumentKey key = new UserInstrumentKey();
                setUserInstrumentKey(key, id, iid);
                UserInstruments ui = UserInstruments.builder()
                        .userId(key)
                        .userInfo(user)
                        .instrument(InstrumentNameTable.builder().instrumentId(iid).build()) // reference만 필요
                        .build();
                userInstrumentsRepository.save(ui);
            }
        }
        return user;
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

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일 필터 : 닉네임 ")
    void searchProfilesByNickname() {
        // given
        ensureNameTables(new int[]{901}, new int[]{901}); // 최소 시드 (충돌 방지용 높은 ID)
        createUser("user_nick_1", "alpha", 'M', new Integer[]{901}, new Integer[]{901});
        createUser("user_nick_2", "beta", 'F', new Integer[]{901}, new Integer[]{901});

        var criteria = ProfileSearchCriteria.builder()
                .nickName("alp") // 부분 검색
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 닉네임 필터 결과 수: {}", page.getTotalElements());

        // then
        assertFalse(page.isEmpty(), "검색 결과가 비어있지 않아야 합니다");
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_nick_1"), "alpha 유저가 포함되어야 합니다");
        assertFalse(ids.contains("user_nick_2"), "beta 유저는 제외되어야 합니다");
    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일 필터 : 성별 ")
    void searchProfilesBySex() {
        // given
        ensureNameTables(new int[]{902}, new int[]{902});
        createUser("user_sex_m", "m_user", 'M', new Integer[]{902}, new Integer[]{902});
        createUser("user_sex_f", "f_user", 'F', new Integer[]{902}, new Integer[]{902});

        var criteria = ProfileSearchCriteria.builder()
                .sex('M')
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 성별(M) 필터 결과 수: {}", page.getTotalElements());

        // then
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_sex_m"), "남성 유저가 포함되어야 합니다");
        assertFalse(ids.contains("user_sex_f"), "여성 유저는 제외되어야 합니다");
    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일필터, 단일속성: Genre ")
    void searchProfilesByGenre() {
        // given
        ensureNameTables(new int[]{903, 904}, new int[]{903});
        createUser("user_g1", "g1_user", 'M', new Integer[]{903}, new Integer[]{903});
        createUser("user_g2", "g2_user", 'F', new Integer[]{904}, new Integer[]{903});

        var criteria = ProfileSearchCriteria.builder()
                .genres(List.of(903))
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 장르(903) 필터 결과 수: {}", page.getTotalElements());

        // then
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_g1"), "장르 903 유저가 포함되어야 합니다");
        assertFalse(ids.contains("user_g2"), "다른 장르 유저는 제외되어야 합니다");
    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일필터, 다중 속성 : Genres")
    void searchProfilesByGenres() {
        // given
        ensureNameTables(new int[]{905, 906}, new int[]{905});
        createUser("user_g905", "g905", 'M', new Integer[]{905}, new Integer[]{905});
        createUser("user_g906", "g906", 'F', new Integer[]{906}, new Integer[]{905});
        createUser("user_g_none", "g0", 'M', new Integer[]{}, new Integer[]{905});

        var criteria = ProfileSearchCriteria.builder()
                .genres(List.of(905, 906)) // 둘 중 하나라도 매칭되면 조회됨
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 장르(905,906) 필터 결과 수: {}", page.getTotalElements());

        // then
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_g905"));
        assertTrue(ids.contains("user_g906"));
        assertFalse(ids.contains("user_g_none"));
    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일 필터, 다중 속성 : instruments")
    void searchProfilesByInstruments() {
        // given
        ensureNameTables(new int[]{907}, new int[]{907, 908});
        createUser("user_i907", "i907", 'M', new Integer[]{907}, new Integer[]{907});
        createUser("user_i908", "i908", 'F', new Integer[]{907}, new Integer[]{908});
        createUser("user_i_none", "i0", 'M', new Integer[]{907}, new Integer[]{});

        var criteria = ProfileSearchCriteria.builder()
                .instruments(List.of(907, 908)) // 둘 중 하나라도 매칭되면 조회됨
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 악기(907,908) 필터 결과 수: {}", page.getTotalElements());

        // then
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_i907"));
        assertTrue(ids.contains("user_i908"));
        assertFalse(ids.contains("user_i_none"));
    }

    @Test
    @DisplayName("페이징 기반 유저 정보 찾기 : 단일필터, 단일 속성 : instrument")
    void searchProfilesByInstrument() {
        // given
        ensureNameTables(new int[]{909}, new int[]{909, 910});
        createUser("user_ins_909", "ins909", 'M', new Integer[]{909}, new Integer[]{909});
        createUser("user_ins_910", "ins910", 'F', new Integer[]{909}, new Integer[]{910});

        var criteria = ProfileSearchCriteria.builder()
                .instruments(List.of(909))
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 악기(909) 단일 필터 결과 수: {}", page.getTotalElements());

        // then
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_ins_909"));
        assertFalse(ids.contains("user_ins_910"));
    }


    @Test
    @DisplayName("유저 정보 찾기 : 다중 필터 , nickname, genres, instruments")
    void searchProfilesByMultipleFilters() {
        // given
        ensureNameTables(new int[]{911, 912}, new int[]{911, 912});
        // 조건: nickname에 "mix" 포함, 장르 중 911 포함, 악기 중 912 포함
        createUser("user_mix_ok", "mix_alpha", 'M', new Integer[]{911}, new Integer[]{912}); // 매칭 O
        createUser("user_mix_bad_nick", "other", 'M', new Integer[]{911}, new Integer[]{912}); // 닉네임 불일치
        createUser("user_mix_bad_genre", "mix_beta", 'M', new Integer[]{912}, new Integer[]{912}); // 장르 불일치
        createUser("user_mix_bad_instr", "mix_gamma", 'M', new Integer[]{911}, new Integer[]{911}); // 악기 불일치

        var criteria = ProfileSearchCriteria.builder()
                .nickName("mix")
                .genres(List.of(911))
                .instruments(List.of(912))
                .build();

        // when
        Page<UserResponse> page = profileSearchService.searchProfiles(criteria, PageRequest.of(0, 10));
        log.info("[검증 로그] 다중필터 결과 수: {}", page.getTotalElements());

        // then
        List<String> ids = page.map(UserResponse::getUserId).getContent();
        assertTrue(ids.contains("user_mix_ok"));
        assertFalse(ids.contains("user_mix_bad_nick"));
        assertFalse(ids.contains("user_mix_bad_genre"));
        assertFalse(ids.contains("user_mix_bad_instr"));
    }

    @Test
    @DisplayName("유저 정보 찾기 커서 기반 : userId 내림차순, 중복 없이 다음 페이지 조회")
    void searchProfilesByCursor() {
        // given: userId가 사전순 내림차순으로 정렬될 수 있도록 생성
        ensureNameTables(new int[]{920}, new int[]{920});
        createUser("user_cursor_001", "c001", 'M', new Integer[]{920}, new Integer[]{920});
        createUser("user_cursor_002", "c002", 'M', new Integer[]{920}, new Integer[]{920});
        createUser("user_cursor_003", "c003", 'M', new Integer[]{920}, new Integer[]{920});
        createUser("user_cursor_004", "c004", 'M', new Integer[]{920}, new Integer[]{920});
        createUser("user_cursor_005", "c005", 'M', new Integer[]{920}, new Integer[]{920});

        // 아무 필터 없이 커서 기반 조회 (size=2)
        var criteria = ProfileSearchCriteria.builder().build();

        // 1페이지
        var slice1 = profileSearchService.searchProfilesByCursor(criteria, null, 2);
        List<UserResponse> page1 = slice1.getContent();
        log.info("[커서1] size={}, hasNext={}, ids={}", page1.size(), slice1.hasNext(),
                page1.stream().map(UserResponse::getUserId).toList());
        assertTrue(page1.size() <= 2);
        assertTrue(slice1.hasNext());

        String nextCursor = page1.get(page1.size() - 1).getUserId();

        // 2페이지 (nextCursor 기준으로 이어받기)
        var slice2 = profileSearchService.searchProfilesByCursor(criteria, nextCursor, 2);
        List<UserResponse> page2 = slice2.getContent();
        log.info("[커서2] size={}, hasNext={}, ids={}", page2.size(), slice2.hasNext(),
                page2.stream().map(UserResponse::getUserId).toList());

        // 중복 없음 확인
        List<String> ids1 = page1.stream().map(UserResponse::getUserId).toList();
        List<String> ids2 = page2.stream().map(UserResponse::getUserId).toList();
        for (String id : ids1) {
            assertFalse(ids2.contains(id), "커서 페이지 간 중복이 없어야 합니다");
        }

        // 내림차순 정렬 확인 (각 페이지 내)
        assertTrue(isDescByUserId(ids1));
        assertTrue(isDescByUserId(ids2));
    }

    // 유틸: 문자열 비교로 내림차순인지 확인
    private static boolean isDescByUserId(List<String> ids) {
        for (int i = 1; i < ids.size(); i++) {
            if (ids.get(i - 1).compareTo(ids.get(i)) < 0) return false;
        }
        return true;
    }

    @Test
    void searchProfile() {
    }

    @Test
    void searchProfiles() {
        createUser("user_nick_1", "alpha", 'M', new Integer[]{901}, new Integer[]{901});
        UserResponse userInfoResponse = profileSearchService.searchProfileById("user_nick_1");
        assertNotNull(userInfoResponse);
        assertEquals("user_nick_1", userInfoResponse.getUserId());
        assertEquals("alpha", userInfoResponse.getNickname());
        assertTrue(userInfoResponse.getInstruments().contains("INSTRUMENT_901"));
        assertTrue(userInfoResponse.getGenres().contains("GENRE_901"));
    }
}
