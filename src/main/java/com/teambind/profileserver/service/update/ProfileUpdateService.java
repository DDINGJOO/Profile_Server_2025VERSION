package com.teambind.profileserver.service.update;


import com.teambind.profileserver.dto.request.HistoryUpdateRequest;
import com.teambind.profileserver.entity.UserGenres;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.UserInstruments;
import com.teambind.profileserver.entity.key.UserGenreKey;
import com.teambind.profileserver.entity.key.UserInstrumentKey;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.*;
import com.teambind.profileserver.service.history.UserProfileHistoryService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    private final UserInfoRepository userInfoRepository;
    private final UserGenresRepository userGenresRepository;
    private final UserInstrumentsRepository userInstrumentsRepository;
    private final InstrumentNameTableRepository instrumentNameTableRepository;
    private final GenreNameTableRepository genreNameTableRepository;
    private final UserProfileHistoryService historyService;


    @Transactional
    public void UserProfileImageUpdate(String userId, String imageUrl)
    {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow(
                () -> new ProfileException(ErrorCode.USER_NOT_FOUND)
        );
        userInfo.setProfileImageUrl(imageUrl);
    }
    @Transactional
    public UserInfo updateProfile(String userId, String nickname, List<Integer> instruments, List<Integer> genres, boolean isChattable, boolean isPublicProfile) {

        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();
        // 닉네임이 null이 아니고 변경된 경우에만 업데이트
        if (nickname != null && !nickname.equals(userInfo.getNickname())) {
            if(userInfoRepository.existsByNickname(nickname))
            {
                throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            userInfo.setNickname(nickname);
        }

        userInfo.setIsChatable(isChattable);
        userInfo.setIsPublic(isPublicProfile);

        // 악기 목록이 제공된 경우에만 업데이트 (null이면 변경 없음, 빈 리스트면 전체 삭제)
        if (instruments != null) {
            Set<Integer> desiredInstruments = new HashSet<>(instruments);
            List<Integer> currentInstList = userInstrumentsRepository.findInstrumentIdsByUserId(userId);
            Set<Integer> currentInstruments = new HashSet<>(currentInstList);

            // 변경해야 할 항목 계산
            Set<Integer> toRemove = new HashSet<>(currentInstruments);
            toRemove.removeAll(desiredInstruments);

            Set<Integer> toAdd = new HashSet<>(desiredInstruments);
            toAdd.removeAll(currentInstruments);

            if (!toRemove.isEmpty()) {
                userInstrumentsRepository.deleteByUserIdAndInstrumentIdsIn(userId, toRemove);
            } else if (desiredInstruments.isEmpty() && !currentInstruments.isEmpty()) {
                // 원하는 값이 비어 있으면 전체 삭제
                userInstrumentsRepository.deleteByUserId(userId);
            }

            if (!toAdd.isEmpty()) {
                List<UserInstruments> uiBatch = new ArrayList<>(toAdd.size());
                // Use references to satisfy @MapsId relations without hitting DB for full entities
                UserInfo userRef = userInfoRepository.getReferenceById(userId);
                for (Integer instId : toAdd) {
                    uiBatch.add(UserInstruments.builder()
                            .userId(new UserInstrumentKey(userId, instId))
                            .userInfo(userRef)
                            .instrument(instrumentNameTableRepository.getReferenceById(instId))
                            .build());
                }
                userInstrumentsRepository.saveAll(uiBatch);
            }

        }

        // 장르 목록이 제공된 경우에만 업데이트 (null이면 변경 없음, 빈 리스트면 전체 삭제)
        if (genres != null) {
            Set<Integer> desiredGenres = new HashSet<>(genres);
            List<Integer> currentGenreList = userGenresRepository.findGenreIdsByUserId(userId);
            Set<Integer> currentGenres = new HashSet<>(currentGenreList);

            // 변경해야 할 항목 계산
            Set<Integer> toRemove = new HashSet<>(currentGenres);
            toRemove.removeAll(desiredGenres);

            Set<Integer> toAdd = new HashSet<>(desiredGenres);
            toAdd.removeAll(currentGenres);

            if (!toRemove.isEmpty()) {
                userGenresRepository.deleteByUserIdAndGenreIdsIn(userId, toRemove);
            } else if (desiredGenres.isEmpty() && !currentGenres.isEmpty()) {
                // 원하는 값이 비어 있으면 전체 삭제
                userGenresRepository.deleteByUserId(userId);
            }

            if (!toAdd.isEmpty()) {
                List<UserGenres> ugBatch = new ArrayList<>(toAdd.size());
                UserInfo userRef = userInfoRepository.getReferenceById(userId);
                for (Integer genreId : toAdd) {
                    ugBatch.add(UserGenres.builder()
                            .userId(new UserGenreKey(userId, genreId))
                            .userInfo(userRef)
                            .genre(genreNameTableRepository.getReferenceById(genreId))
                            .build());
                }
                userGenresRepository.saveAll(ugBatch);
            }
        }

        // 사용자 정보 변경사항 저장(닉네임)
        // 엔티티는 영속 상태이므로 save가 없어도 되지만, 가독성을 위해 명시적으로 호출
        historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                HistoryUpdateRequest.builder()
                        .columnName("nickname")
                        .oldValue(userInfo.getNickname())
                        .newValue(nickname)
                        .build()
        });
        userInfoRepository.save(userInfo);

        // 추가 조회와 불필요한 지연 로딩을 피하기 위해 영속 엔티티 반환
        return userInfo;
    }

    @Transactional
    public UserInfo updateProfileAll(String userId, String nickname, List<Integer> instruments, List<Integer> genres, boolean isChattable, boolean isPublicProfile)  {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow();

        // 1) 닉네임은 전체 데이터 갱신 요구사항에 따라 전달된 값으로 그대로 반영
        //    (null 허용 정책이 별도로 없다면 null이면 기존 값 유지로 처리)
        if (nickname != null && !nickname.equals(userInfo.getNickname())) {
            if (userInfoRepository.existsByNickname(nickname)) {
                throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            userInfo.setNickname(nickname);
            userInfo.setIsChatable(isChattable);
            userInfo.setIsPublic(isPublicProfile);
            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("nickname")
                            .oldValue(userInfo.getNickname())
                            .newValue(nickname)
                            .build()
            });
        }


        // 2) 악기/장르 모두 전체 갱신: 기존 것을 모두 삭제하고, 전달된 전체 목록을 넣는다
        //    repositories에 있는 bulk delete를 사용해 효율적으로 삭제
        userInstrumentsRepository.deleteByUserId(userId);
        userGenresRepository.deleteByUserId(userId);

        // 3) 전달된 전체 목록을 일괄 저장 (null은 빈 목록으로 간주)
        if (instruments != null && !instruments.isEmpty()) {
            List<UserInstruments> uiBatch = new ArrayList<>(instruments.size());
            UserInfo userRef = userInfoRepository.getReferenceById(userId);
            for (Integer instId : instruments) {
                uiBatch.add(UserInstruments.builder()
                        .userId(new UserInstrumentKey(userId, instId))
                        .userInfo(userRef)
                        .instrument(instrumentNameTableRepository.getReferenceById(instId))
                        .build());
            }
            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("instruments")
                            .newValue(instruments.toString())
                            .build()
            });
            userInstrumentsRepository.saveAll(uiBatch);
        }

        if (genres != null && !genres.isEmpty()) {
            List<UserGenres> ugBatch = new ArrayList<>(genres.size());
            UserInfo userRef = userInfoRepository.getReferenceById(userId);
            for (Integer genreId : genres) {
                ugBatch.add(UserGenres.builder()
                        .userId(new UserGenreKey(userId, genreId))
                        .userInfo(userRef)
                        .genre(genreNameTableRepository.getReferenceById(genreId))
                        .build());
            }
            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("genres")
                            .newValue(genres.toString())
                            .build()
            });
            userGenresRepository.saveAll(ugBatch);
        }

        // 4) 사용자 정보 저장 (엔티티는 영속 상태이지만 명시적으로 저장하여 의도를 드러냄)
        userInfoRepository.save(userInfo);

        // 5) 영속 엔티티 반환
        return userInfo;
    }




    @Transactional
    public UserInfo updateProfileImage(String userId, String imageUrl)  {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow(
                () -> new ProfileException(ErrorCode.USER_NOT_FOUND)
        );
        userInfo.setProfileImageUrl(imageUrl);
        userInfoRepository.save(userInfo);


        if (!historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                HistoryUpdateRequest.builder()
                        .columnName("profileImageUrl")
                        .oldValue(userInfo.getProfileImageUrl())
                        .newValue(imageUrl)
                        .build()
        })){
            throw new ProfileException(ErrorCode.HISTORY_UPDATE_FAILED);
        }

        return userInfo;
    }
}
