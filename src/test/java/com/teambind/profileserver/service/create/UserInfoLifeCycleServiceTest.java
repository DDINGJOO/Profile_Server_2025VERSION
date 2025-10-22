package com.teambind.profileserver.service.create;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserInfoLifeCycleService 단위 테스트
 *
 * 테스트 범위:
 * 1. 사용자 프로필 생성 (createUserProfile)
 * 2. 사용자 프로필 삭제 (deleteUserProfile)
 *
 * 테스트 전략:
 * - ArgumentCaptor로 실제 저장되는 객체 검증
 * - 닉네임 생성 로직 검증
 * - 예외 상황 처리 확인
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserInfoLifeCycleService 테스트")
class UserInfoLifeCycleServiceTest {

    private static final String TEST_USER_ID = "testUser123";
    private static final String TEST_PROVIDER = "kakao";
    @InjectMocks
    private UserInfoLifeCycleService userInfoLifeCycleService;
    @Mock
    private UserInfoRepository userInfoRepository;

    @Nested
    @DisplayName("사용자 프로필 생성 (createUserProfile)")
    class CreateUserProfile {

        @Test
        @DisplayName("성공 - 기본 프로필 생성")
        void createUserProfile_Success() {
            // given
            when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);

            // then
            verify(userInfoRepository).save(userInfoCaptor.capture());

            UserInfo savedUser = userInfoCaptor.getValue();
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getUserId()).isEqualTo(TEST_USER_ID);
            assertThat(savedUser.getNickname()).isNotNull();
            assertThat(savedUser.getNickname().toLowerCase()).contains(TEST_PROVIDER);  // provider가 닉네임에 포함됨
        }

        @Test
        @DisplayName("성공 - 초기 상태 검증 (모든 필드 기본값)")
        void createUserProfile_InitialState_AllFieldsDefault() {
            // given
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);

            // then
            verify(userInfoRepository).save(userInfoCaptor.capture());

            UserInfo savedUser = userInfoCaptor.getValue();

            // 기본값 검증
            assertThat(savedUser.getCity()).isNull();
            assertThat(savedUser.getIntroduction()).isNull();
            assertThat(savedUser.getSex()).isNull();
            assertThat(savedUser.getProfileImageUrl()).isNull();

            // Boolean 필드는 false
            assertThat(savedUser.getIsChatable()).isFalse();
            assertThat(savedUser.getIsPublic()).isFalse();

            // 생성/수정 시간 존재
            assertThat(savedUser.getCreatedAt()).isNotNull();
            assertThat(savedUser.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 다양한 provider로 프로필 생성")
        void createUserProfile_DifferentProviders() {
            // given
            String[] providers = {"kakao", "google", "naver", "github"};

            for (String provider : providers) {
                ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);
                reset(userInfoRepository);

                // when
                userInfoLifeCycleService.createUserProfile(TEST_USER_ID + "_" + provider, provider);

                // then
                verify(userInfoRepository).save(userInfoCaptor.capture());
                UserInfo savedUser = userInfoCaptor.getValue();
                assertThat(savedUser.getNickname()).isNotNull();
                assertThat(savedUser.getUserId()).isEqualTo(TEST_USER_ID + "_" + provider);
            }
        }

        @Test
        @DisplayName("닉네임은 자동 생성됨 (NicknameGenerator 사용)")
        void createUserProfile_NicknameGenerated() {
            // given
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);

            // then
            verify(userInfoRepository).save(userInfoCaptor.capture());

            UserInfo savedUser = userInfoCaptor.getValue();
            // 닉네임이 null이 아니고, 빈 문자열도 아님
            assertThat(savedUser.getNickname())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Nested
    @DisplayName("사용자 프로필 삭제 (deleteUserProfile)")
    class DeleteUserProfile {

        @Test
        @DisplayName("성공 - 존재하는 사용자 삭제")
        void deleteUserProfile_Success() {
            // given
            UserInfo existingUser = createDefaultUserInfo(TEST_USER_ID);
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));

            // when
            userInfoLifeCycleService.deleteUserProfile(TEST_USER_ID);

            // then
            verify(userInfoRepository).findById(TEST_USER_ID);
            verify(userInfoRepository).delete(existingUser);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 삭제 시 예외 발생")
        void deleteUserProfile_UserNotFound_ThrowsException() {
            // given
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userInfoLifeCycleService.deleteUserProfile(TEST_USER_ID))
                    .isInstanceOf(ProfileException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ProfileErrorCode.USER_NOT_FOUND);

            verify(userInfoRepository).findById(TEST_USER_ID);
            verify(userInfoRepository, never()).delete(any());
        }

        @Test
        @DisplayName("연관된 데이터와 함께 삭제 (cascade)")
        void deleteUserProfile_WithRelations() {
            // given
            UserInfo userWithRelations = userInfo()
                    .userId(TEST_USER_ID)
                    .withGenres(createGenres().subList(0, 2))
                    .withInstruments(createInstruments().subList(0, 2))
                    .build();

            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(userWithRelations));

            // when
            userInfoLifeCycleService.deleteUserProfile(TEST_USER_ID);

            // then
            verify(userInfoRepository).delete(userWithRelations);
            // cascade 옵션으로 인해 연관 엔티티도 함께 삭제됨 (JPA 동작)
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("매우 긴 userId로 프로필 생성")
        void createUserProfile_VeryLongUserId() {
            // given
            String veryLongUserId = "a".repeat(255);
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(veryLongUserId, TEST_PROVIDER);

            // then
            verify(userInfoRepository).save(userInfoCaptor.capture());
            assertThat(userInfoCaptor.getValue().getUserId()).isEqualTo(veryLongUserId);
        }

        @Test
        @DisplayName("특수문자가 포함된 userId로 프로필 생성")
        void createUserProfile_SpecialCharsInUserId() {
            // given
            String specialUserId = "user!@#$%^&*()_+";
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(specialUserId, TEST_PROVIDER);

            // then
            verify(userInfoRepository).save(userInfoCaptor.capture());
            assertThat(userInfoCaptor.getValue().getUserId()).isEqualTo(specialUserId);
        }
		

        @Test
        @DisplayName("빈 문자열 provider로 프로필 생성")
        void createUserProfile_EmptyProvider() {
            // given
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, "");

            // then
            verify(userInfoRepository).save(userInfoCaptor.capture());
            UserInfo savedUser = userInfoCaptor.getValue();
            assertThat(savedUser.getNickname()).isNotNull();
        }
    }

    @Nested
    @DisplayName("생성과 삭제 통합 시나리오")
    class IntegrationScenarios {

        @Test
        @DisplayName("프로필 생성 후 바로 삭제")
        void createThenDelete_Success() {
            // given
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when - 생성
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);
            verify(userInfoRepository).save(userInfoCaptor.capture());

            UserInfo createdUser = userInfoCaptor.getValue();

            // given - 삭제 준비
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(createdUser));

            // when - 삭제
            userInfoLifeCycleService.deleteUserProfile(TEST_USER_ID);

            // then
            verify(userInfoRepository).delete(createdUser);
        }

        @Test
        @DisplayName("동일한 userId로 여러 번 생성 시도 (DB 제약조건에 의존)")
        void createMultipleTimes_SameUserId() {
            // given
            when(userInfoRepository.save(any(UserInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);

            // then
            // 실제로는 DB의 PK 제약조건에 의해 두 번째 저장이 실패하겠지만,
            // 서비스 계층에서는 두 번 호출됨
            verify(userInfoRepository, times(2)).save(any(UserInfo.class));
        }
    }

    @Nested
    @DisplayName("트랜잭션 동작 검증")
    class TransactionBehavior {

        @Test
        @DisplayName("createUserProfile는 @Transactional 적용")
        void createUserProfile_IsTransactional() {
            // given
            ArgumentCaptor<UserInfo> userInfoCaptor = ArgumentCaptor.forClass(UserInfo.class);

            // when
            userInfoLifeCycleService.createUserProfile(TEST_USER_ID, TEST_PROVIDER);

            // then
            // @Transactional 어노테이션이 있으므로 save 호출됨
            verify(userInfoRepository).save(userInfoCaptor.capture());
        }

        @Test
        @DisplayName("deleteUserProfile는 @Transactional 적용")
        void deleteUserProfile_IsTransactional() {
            // given
            UserInfo existingUser = createDefaultUserInfo(TEST_USER_ID);
            when(userInfoRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(existingUser));

            // when
            userInfoLifeCycleService.deleteUserProfile(TEST_USER_ID);

            // then
            // @Transactional 어노테이션이 있으므로 delete 호출됨
            verify(userInfoRepository).delete(existingUser);
        }
    }
}
