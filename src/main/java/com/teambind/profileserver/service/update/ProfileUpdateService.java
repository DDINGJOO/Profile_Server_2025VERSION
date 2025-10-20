package com.teambind.profileserver.service.update;


import com.teambind.profileserver.dto.request.HistoryUpdateRequest;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.GenreNameTableRepository;
import com.teambind.profileserver.repository.InstrumentNameTableRepository;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.service.history.UserProfileHistoryService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    private final UserInfoRepository userInfoRepository;
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
    /**
     * 프로필을 부분 업데이트합니다. (PATCH 방식)
     * request에서 null이 아닌 필드만 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param request 업데이트할 프로필 정보
     * @return 업데이트된 UserInfo
     */
    @Transactional
    public UserInfo updateProfile(String userId, ProfileUpdateRequest request) {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow(
                () -> new ProfileException(ErrorCode.USER_NOT_FOUND)
        );

        // 닉네임이 null이 아니고 변경된 경우에만 업데이트
        if (request.getNickname() != null && !request.getNickname().equals(userInfo.getNickname())) {
            if(userInfoRepository.existsByNickname(request.getNickname())) {
                throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }

            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("nickname")
                            .oldValue(userInfo.getNickname())
                            .newValue(request.getNickname())
                            .build()
            });

            userInfo.setNickname(request.getNickname());
        }

        // 기타 필드 업데이트 (null이 아닌 경우)
        if (request.getIntroduction() != null) {
            userInfo.setIntroduction(request.getIntroduction());
        }
        if (request.getCity() != null) {
            userInfo.setCity(request.getCity());
        }
        if (request.getSex() != null) {
            userInfo.setSex(request.getSex());
        }

        userInfo.setIsChatable(request.isChattable());
        userInfo.setIsPublic(request.isPublicProfile());

        // 악기 목록 업데이트 (연관관계 편의 메소드 사용)
        updateAttributesUsingRelation(
                userInfo,
                request.getInstruments(),
                user -> user.getUserInstruments().stream()
                        .map(ui -> ui.getAttribute().getInstrumentId())
                        .collect(Collectors.toSet()),
                instrumentNameTableRepository::getReferenceById,
                UserInfo::addInstrument,
                UserInfo::removeInstrument
        );

        // 장르 목록 업데이트 (연관관계 편의 메소드 사용)
        updateAttributesUsingRelation(
                userInfo,
                request.getGenres(),
                user -> user.getUserGenres().stream()
                        .map(ug -> ug.getAttribute().getGenreId())
                        .collect(Collectors.toSet()),
                genreNameTableRepository::getReferenceById,
                UserInfo::addGenre,
                UserInfo::removeGenre
        );

        userInfoRepository.save(userInfo);

        // 추가 조회와 불필요한 지연 로딩을 피하기 위해 영속 엔티티 반환
        return userInfo;
    }

    /**
     * 프로필을 전체 업데이트합니다. (PUT 방식)
     * 모든 필드를 request의 값으로 교체합니다.
     *
     * @param userId 사용자 ID
     * @param request 업데이트할 프로필 정보
     * @return 업데이트된 UserInfo
     */
    @Transactional
    public UserInfo updateProfileAll(String userId, ProfileUpdateRequest request) {
        UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow(
                () -> new ProfileException(ErrorCode.USER_NOT_FOUND));

        // 닉네임은 전체 데이터 갱신 요구사항에 따라 전달된 값으로 그대로 반영
        if (request.getNickname() != null && !request.getNickname().equals(userInfo.getNickname())) {
            if (userInfoRepository.existsByNickname(request.getNickname())) {
                throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }

            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("nickname")
                            .oldValue(userInfo.getNickname())
                            .newValue(request.getNickname())
                            .build()
            });

            userInfo.setNickname(request.getNickname());
        }

        userInfo.setIntroduction(request.getIntroduction());
        userInfo.setCity(request.getCity());
        userInfo.setSex(request.getSex());
        userInfo.setIsChatable(request.isChattable());
        userInfo.setIsPublic(request.isPublicProfile());

        // 악기 목록 전체 교체 (연관관계 편의 메소드 사용)
        replaceAttributesUsingRelation(
                userInfo,
                request.getInstruments(),
                instrumentNameTableRepository::getReferenceById,
                userInfo::clearInstruments,
                UserInfo::addInstrument
        );

        // 장르 목록 전체 교체 (연관관계 편의 메소드 사용)
        replaceAttributesUsingRelation(
                userInfo,
                request.getGenres(),
                genreNameTableRepository::getReferenceById,
                userInfo::clearGenres,
                UserInfo::addGenre
        );

        // 히스토리 저장
        if (request.getInstruments() != null && !request.getInstruments().isEmpty()) {
            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("instruments")
                            .newValue(request.getInstruments().toString())
                            .build()
            });
        }

        if (request.getGenres() != null && !request.getGenres().isEmpty()) {
            historyService.saveAllHistory(userInfo, new HistoryUpdateRequest[]{
                    HistoryUpdateRequest.builder()
                            .columnName("genres")
                            .newValue(request.getGenres().toString())
                            .build()
            });
        }

        userInfoRepository.save(userInfo);

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

    /**
     * 사용자 속성을 부분 업데이트 (연관관계 편의 메소드 활용)
     * @param userInfo 영속 상태의 UserInfo 엔티티
     * @param desiredIds 원하는 속성 ID 목록 (null이면 변경 없음, 빈 리스트면 전체 삭제)
     * @param currentAttributesExtractor 현재 속성 목록에서 ID 추출 함수
     * @param nameTableFetcher ID로 NameTable 엔티티 조회 함수
     * @param addMethod 속성 추가 메소드 (userInfo::addGenre)
     * @param removeMethod 속성 제거 메소드 (userInfo::removeGenre)
     * @param <T> NameTable 타입 (GenreNameTable, InstrumentNameTable 등)
     */
    private <T> void updateAttributesUsingRelation(
            UserInfo userInfo,
            List<Integer> desiredIds,
            Function<UserInfo, Set<Integer>> currentAttributesExtractor,
            Function<Integer, T> nameTableFetcher,
            BiConsumer<UserInfo, T> addMethod,
            BiConsumer<UserInfo, T> removeMethod
    ) {
        if (desiredIds == null) {
            return; // null이면 변경 없음
        }

        Set<Integer> desiredSet = new HashSet<>(desiredIds);
        Set<Integer> currentSet = currentAttributesExtractor.apply(userInfo);

        // 삭제할 항목 계산
        Set<Integer> toRemove = new HashSet<>(currentSet);
        toRemove.removeAll(desiredSet);

        // 추가할 항목 계산
        Set<Integer> toAdd = new HashSet<>(desiredSet);
        toAdd.removeAll(currentSet);

        // 제거 처리 (연관관계 편의 메소드 사용)
        for (Integer id : toRemove) {
            T nameTable = nameTableFetcher.apply(id);
            removeMethod.accept(userInfo, nameTable);
        }

        // 추가 처리 (연관관계 편의 메소드 사용)
        for (Integer id : toAdd) {
            T nameTable = nameTableFetcher.apply(id);
            addMethod.accept(userInfo, nameTable);
        }
        // cascade로 자동 저장됨
    }

    /**
     * 사용자 속성을 전체 교체 (연관관계 편의 메소드 활용)
     * @param userInfo 영속 상태의 UserInfo 엔티티
     * @param desiredIds 속성 ID 목록 (null 또는 빈 리스트면 전체 삭제)
     * @param nameTableFetcher ID로 NameTable 엔티티 조회 함수
     * @param clearMethod 전체 삭제 메소드 (userInfo::clearGenres)
     * @param addMethod 속성 추가 메소드 (userInfo::addGenre)
     * @param <T> NameTable 타입 (GenreNameTable, InstrumentNameTable 등)
     */
    private <T> void replaceAttributesUsingRelation(
            UserInfo userInfo,
            List<Integer> desiredIds,
            Function<Integer, T> nameTableFetcher,
            Runnable clearMethod,
            BiConsumer<UserInfo, T> addMethod
    ) {
        // 기존 것 모두 삭제 (연관관계 편의 메소드 사용)
        clearMethod.run();

        // 새로운 것 추가
        if (desiredIds != null && !desiredIds.isEmpty()) {
            for (Integer id : desiredIds) {
                T nameTable = nameTableFetcher.apply(id);
                addMethod.accept(userInfo, nameTable);
            }
        }
        // cascade로 자동 저장됨
    }
}
