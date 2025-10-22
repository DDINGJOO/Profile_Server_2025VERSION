package com.teambind.profileserver.service.update;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.events.event.UserNickNameChangedEvent;
import com.teambind.profileserver.events.publisher.EventPublisher;
import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.utils.InitTableMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProfileUpdateService 단위 테스트
 *
 * 테스트 전략:
 * 1. Mockito를 활용한 단위 테스트
 * 2. Given-When-Then 패턴 사용
 * 3. DisplayName으로 한글 명세 작성
 * 4. Factory 패턴으로 테스트 데이터 생성
 * 5. 엣지 케이스 및 예외 상황 커버
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileUpdateService 테스트")
class ProfileUpdateServiceTest {

    private static final String TEST_USER_ID = "testUser123";
    @InjectMocks
    private ProfileUpdateService profileUpdateService;
    @Mock
    private UserInfoRepository userInfoRepository;
    @Mock
    private EventPublisher eventPublisher;
    private UserInfo testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 장르/악기/지역 데이터 초기화 (static Map 사용)
        InitTableMapper.genreNameTable = new java.util.HashMap<>();
        InitTableMapper.instrumentNameTable = new java.util.HashMap<>();
        InitTableMapper.locationNamesTable = new java.util.HashMap<>();

        createGenres().forEach(genre ->
                InitTableMapper.genreNameTable.put(genre.getGenreId(), genre));
        createInstruments().forEach(instrument ->
                InitTableMapper.instrumentNameTable.put(instrument.getInstrumentId(), instrument));

        // 테스트용 지역 데이터 초기화
        InitTableMapper.locationNamesTable.put("SEOUL", "서울");
        InitTableMapper.locationNamesTable.put("BUSAN", "부산");
        InitTableMapper.locationNamesTable.put("DAEGU", "대구");
        InitTableMapper.locationNamesTable.put("INCHEON", "인천");

        // 기본 테스트 사용자 생성
        testUser = createDefaultUserInfo(TEST_USER_ID);
    }

    @Nested
    @DisplayName("프로필 업데이트 - 기본 필드")
    class UpdateBasicFields {

        @Test
        @DisplayName("닉네임만 변경 성공")
        void updateNickname_Success() {
            // given
            String newNickname = "newNickname";
            ProfileUpdateRequest request = updateRequest()
                    .nickname(newNickname)
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userInfoRepository.existsByNickname(newNickname)).thenReturn(false);

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getNickname()).isEqualTo(newNickname);
            verify(userInfoRepository).save(testUser);
            verify(eventPublisher).publish(any(UserNickNameChangedEvent.class));
        }

        @Test
        @DisplayName("닉네임 중복 시 예외 발생")
        void updateNickname_Duplicate_ThrowsException() {
            // given
            String duplicateNickname = "existingNickname";
            ProfileUpdateRequest request = updateRequest()
                    .nickname(duplicateNickname)
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userInfoRepository.existsByNickname(duplicateNickname)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> profileUpdateService.updateProfile(TEST_USER_ID, request))
                    .isInstanceOf(ProfileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProfileErrorCode.NICKNAME_ALREADY_EXISTS);

            verify(eventPublisher, never()).publish(any(UserNickNameChangedEvent.class));
        }

        @Test
        @DisplayName("동일한 닉네임으로 변경 시 이벤트 발행 안 함")
        void updateNickname_Same_NoEventPublished() {
            // given
            String sameNickname = testUser.getNickname();
            ProfileUpdateRequest request = updateRequest()
                    .nickname(sameNickname)
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            verify(userInfoRepository, never()).existsByNickname(any());
            verify(eventPublisher, never()).publish(any(UserNickNameChangedEvent.class));
        }

        @Test
        @DisplayName("모든 스칼라 필드 변경 성공")
        void updateAllScalarFields_Success() {
            // given
            ProfileUpdateRequest request = updateRequest()
                    .nickname("newNick")
                    .city("BUSAN")
                    .introduction("새로운 소개")
                    .sex('F')
                    .chattable(true)
                    .publicProfile(true)
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userInfoRepository.existsByNickname("newNick")).thenReturn(false);

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getNickname()).isEqualTo("newNick");
            assertThat(testUser.getCity()).isEqualTo("BUSAN");
            assertThat(testUser.getIntroduction()).isEqualTo("새로운 소개");
            assertThat(testUser.getSex()).isEqualTo('F');
            assertThat(testUser.getIsChatable()).isTrue();
            assertThat(testUser.getIsPublic()).isTrue();
        }

        @Test
        @DisplayName("null 필드는 변경하지 않음 (PATCH 동작)")
        void updateWithNullFields_NoChange() {
            // given
            testUser.setCity("SEOUL");
            testUser.setIntroduction("기존 소개");

            ProfileUpdateRequest request = updateRequest()
                    .nickname("newNick")
                    .introduction(null)  // null은 변경하지 않음
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userInfoRepository.existsByNickname("newNick")).thenReturn(false);

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getNickname()).isEqualTo("newNick");
            assertThat(testUser.getIntroduction()).isEqualTo("기존 소개");  // 변경 안 됨
        }

        @Test
        @DisplayName("지역(city) 변경 성공")
        void updateCity_Success() {
            // given
            testUser.setCity("SEOUL");
            ProfileUpdateRequest request = updateRequest()
                    .city("BUSAN")
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getCity()).isEqualTo("BUSAN");
            verify(userInfoRepository).save(testUser);
        }

        @Test
        @DisplayName("지역(city)을 null로 설정하면 변경하지 않음")
        void updateCity_NullDoesNotChange() {
            // given
            testUser.setCity("SEOUL");
            ProfileUpdateRequest request = updateRequest()
                    .city(null)
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getCity()).isEqualTo("");  // 변경 안 됨
        }

        @Test
        @DisplayName("지역(city)을 다른 지역으로 변경 성공")
        void updateCity_ChangeToAnotherCity_Success() {
            // given
            testUser.setCity("SEOUL");
            ProfileUpdateRequest request = updateRequest()
                    .city("DAEGU")
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getCity()).isEqualTo("DAEGU");
        }
    }

    @Nested
    @DisplayName("프로필 업데이트 - 장르")
    class UpdateGenres {

        @Test
        @DisplayName("장르 새로 추가 성공")
        void addGenres_Success() {
            // given
            ProfileUpdateRequest request = updateRequest()
                    .genres(List.of(1, 2, 3))
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserGenres()).hasSize(3);
            List<Integer> genreIds = testUser.getUserGenres().stream()
                    .map(ug -> ug.getGenre().getGenreId())
                    .toList();
            assertThat(genreIds).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("기존 장르를 다른 장르로 교체")
        void replaceGenres_Success() {
            // given
            testUser.addGenre(createGenre(1, "Rock"));
            testUser.addGenre(createGenre(2, "Jazz"));

            ProfileUpdateRequest request = updateRequest()
                    .genres(List.of(3, 4, 5))  // 완전히 다른 장르
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserGenres()).hasSize(3);
            List<Integer> genreIds = testUser.getUserGenres().stream()
                    .map(ug -> ug.getGenre().getGenreId())
                    .toList();
            assertThat(genreIds).containsExactlyInAnyOrder(3, 4, 5);
        }

        @Test
        @DisplayName("장르 일부 추가")
        void addSomeGenres_Success() {
            // given
            testUser.addGenre(createGenre(1, "Rock"));
            testUser.addGenre(createGenre(2, "Jazz"));

            ProfileUpdateRequest request = updateRequest()
                    .genres(List.of(1, 2, 3))  // 기존 1,2 유지하고 3 추가
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserGenres()).hasSize(3);
            List<Integer> genreIds = testUser.getUserGenres().stream()
                    .map(ug -> ug.getGenre().getGenreId())
                    .toList();
            assertThat(genreIds).containsExactlyInAnyOrder(1, 2, 3);
        }

        @Test
        @DisplayName("장르 일부 제거")
        void removeSomeGenres_Success() {
            // given
            testUser.addGenre(createGenre(1, "Rock"));
            testUser.addGenre(createGenre(2, "Jazz"));
            testUser.addGenre(createGenre(3, "Classical"));

            ProfileUpdateRequest request = updateRequest()
                    .genres(List.of(1, 2))  // 3번 제거
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserGenres()).hasSize(2);
            List<Integer> genreIds = testUser.getUserGenres().stream()
                    .map(ug -> ug.getGenre().getGenreId())
                    .toList();
            assertThat(genreIds).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("장르 전체 삭제 (빈 리스트)")
        void clearGenres_EmptyList() {
            // given
            testUser.addGenre(createGenre(1, "Rock"));
            testUser.addGenre(createGenre(2, "Jazz"));

            ProfileUpdateRequest request = updateRequest()
                    .genres(List.of())  // 빈 리스트
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserGenres()).isEmpty();
        }

        @Test
        @DisplayName("장르 null이면 변경하지 않음 (PATCH 동작)")
        void genresNull_NoChange() {
            // given
            testUser.addGenre(createGenre(1, "Rock"));
            testUser.addGenre(createGenre(2, "Jazz"));

            ProfileUpdateRequest request = updateRequest()
                    .genres(null)  // null은 변경하지 않음
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserGenres()).hasSize(2);  // 기존 유지
        }
    }

    @Nested
    @DisplayName("프로필 업데이트 - 악기")
    class UpdateInstruments {

        @Test
        @DisplayName("악기 새로 추가 성공")
        void addInstruments_Success() {
            // given
            ProfileUpdateRequest request = updateRequest()
                    .instruments(List.of(1, 2))
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserInstruments()).hasSize(2);
            List<Integer> instrumentIds = testUser.getUserInstruments().stream()
                    .map(ui -> ui.getInstrument().getInstrumentId())
                    .toList();
            assertThat(instrumentIds).containsExactlyInAnyOrder(1, 2);
        }

        @Test
        @DisplayName("악기 전체 교체")
        void replaceInstruments_Success() {
            // given
            testUser.addInstrument(createInstrument(1, "Guitar"));
            testUser.addInstrument(createInstrument(2, "Piano"));

            ProfileUpdateRequest request = updateRequest()
                    .instruments(List.of(3, 4))  // 완전히 교체
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserInstruments()).hasSize(2);
            List<Integer> instrumentIds = testUser.getUserInstruments().stream()
                    .map(ui -> ui.getInstrument().getInstrumentId())
                    .toList();
            assertThat(instrumentIds).containsExactlyInAnyOrder(3, 4);
        }

        @Test
        @DisplayName("악기 전체 삭제 (빈 리스트)")
        void clearInstruments_EmptyList() {
            // given
            testUser.addInstrument(createInstrument(1, "Guitar"));

            ProfileUpdateRequest request = updateRequest()
                    .instruments(List.of())  // 빈 리스트
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getUserInstruments()).isEmpty();
        }
    }

    @Nested
    @DisplayName("프로필 업데이트 - 복합 시나리오")
    class ComplexScenarios {

        @Test
        @DisplayName("모든 필드 한 번에 업데이트")
        void updateAllFields_Success() {
            // given
            testUser.addGenre(createGenre(1, "Rock"));
            testUser.addInstrument(createInstrument(1, "Guitar"));

            ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                    .nickname("brandNewNick")
                    .city("DAEGU")
                    .introduction("완전히 새로운 소개")
                    .sex('M')
                    .chattable(true)
                    .publicProfile(true)
                    .genres(List.of(2, 3, 4))
                    .instruments(List.of(2, 3))
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userInfoRepository.existsByNickname("brandNewNick")).thenReturn(false);

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            // 스칼라 필드 검증
            assertThat(testUser.getNickname()).isEqualTo("brandNewNick");
            assertThat(testUser.getCity()).isEqualTo("DAEGU");
            assertThat(testUser.getIntroduction()).isEqualTo("완전히 새로운 소개");
            assertThat(testUser.getSex()).isEqualTo('M');
            assertThat(testUser.getIsChatable()).isTrue();
            assertThat(testUser.getIsPublic()).isTrue();

            // 장르 검증
            assertThat(testUser.getUserGenres()).hasSize(3);
            List<Integer> genreIds = testUser.getUserGenres().stream()
                    .map(ug -> ug.getGenre().getGenreId())
                    .toList();
            assertThat(genreIds).containsExactlyInAnyOrder(2, 3, 4);

            // 악기 검증
            assertThat(testUser.getUserInstruments()).hasSize(2);
            List<Integer> instrumentIds = testUser.getUserInstruments().stream()
                    .map(ui -> ui.getInstrument().getInstrumentId())
                    .toList();
            assertThat(instrumentIds).containsExactlyInAnyOrder(2, 3);

            // 이벤트 발행 검증
            verify(eventPublisher).publish(any(UserNickNameChangedEvent.class));
        }

        @Test
        @DisplayName("최소 필드만 업데이트 (닉네임만)")
        void updateMinimalFields_Success() {
            // given
            ProfileUpdateRequest request = updateRequest()
                    .nickname("minimalNick")
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
            when(userInfoRepository.existsByNickname("minimalNick")).thenReturn(false);

            // when
            profileUpdateService.updateProfile(TEST_USER_ID, request);

            // then
            assertThat(testUser.getNickname()).isEqualTo("minimalNick");
            verify(userInfoRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("프로필 업데이트 - 예외 상황")
    class ExceptionCases {

        @Test
        @DisplayName("존재하지 않는 사용자 - USER_NOT_FOUND")
        void updateProfile_UserNotFound_ThrowsException() {
            // given
            ProfileUpdateRequest request = createBasicUpdateRequest();
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileUpdateService.updateProfile(TEST_USER_ID, request))
                    .isInstanceOf(ProfileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProfileErrorCode.USER_NOT_FOUND);

            verify(userInfoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("프로필 이미지 업데이트")
    class UpdateProfileImage {

        @Test
        @DisplayName("프로필 이미지 URL 변경 성공")
        void updateProfileImage_Success() {
            // given
            String newImageUrl = "https://example.com/new-image.jpg";
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfileImage(TEST_USER_ID, newImageUrl);

            // then
            assertThat(testUser.getProfileImageUrl()).isEqualTo(newImageUrl);
        }

        @Test
        @DisplayName("프로필 이미지 URL null 설정 (이미지 제거)")
        void updateProfileImage_Null() {
            // given
            testUser.setProfileImageUrl("https://example.com/old.jpg");
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

            // when
            profileUpdateService.updateProfileImage(TEST_USER_ID, null);

            // then
            assertThat(testUser.getProfileImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("닉네임 존재 확인")
    class CheckNicknameExists {

        @Test
        @DisplayName("닉네임 존재함")
        void isNickNameExist_True() {
            // given
            String existingNickname = "existingNick";
            when(userInfoRepository.existsByNickname(existingNickname)).thenReturn(true);

            // when
            boolean exists = profileUpdateService.isNickNameExist(existingNickname);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("닉네임 존재하지 않음")
        void isNickNameExist_False() {
            // given
            String newNickname = "newNick";
            when(userInfoRepository.existsByNickname(newNickname)).thenReturn(false);

            // when
            boolean exists = profileUpdateService.isNickNameExist(newNickname);

            // then
            assertThat(exists).isFalse();
        }
    }
}
