package com.teambind.profileserver.validator;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * NickNameValidator 단위 테스트
 *
 * 테스트 전략:
 * 1. Mockito를 활용한 단위 테스트
 * 2. 닉네임 형식 검증 로직 검증
 * 3. 정규식 패턴 검증 (영문, 숫자, 언더스코어, 3-15자)
 * 4. 엣지 케이스 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NickNameValidator 단위 테스트")
class NickNameValidatorTest {

    private NickNameValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new NickNameValidator();
        // 기본 정규식 패턴 설정 (영문, 숫자, 언더스코어, 3-15자)
        ReflectionTestUtils.setField(validator, "regex", "^[a-zA-Z0-9_]{3,15}$");
    }

    @Nested
    @DisplayName("유효한 닉네임 검증")
    class ValidNicknames {

        @Test
        @DisplayName("성공 - 영문 소문자만")
        void validate_LowercaseOnly_Success() {
            // given
            String nickname = "testuser";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 영문 대문자만")
        void validate_UppercaseOnly_Success() {
            // given
            String nickname = "TESTUSER";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 숫자만")
        void validate_NumbersOnly_Success() {
            // given
            String nickname = "123456";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 영문과 숫자 혼합")
        void validate_Alphanumeric_Success() {
            // given
            String nickname = "user123";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 언더스코어 포함")
        void validate_WithUnderscore_Success() {
            // given
            String nickname = "test_user";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 최소 길이 (3자)")
        void validate_MinimumLength_Success() {
            // given
            String nickname = "abc";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 최대 길이 (15자)")
        void validate_MaximumLength_Success() {
            // given
            String nickname = "abcdefghijklmno"; // 15자

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 언더스코어로 시작")
        void validate_StartWithUnderscore_Success() {
            // given
            String nickname = "_testuser";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 언더스코어로 끝남")
        void validate_EndWithUnderscore_Success() {
            // given
            String nickname = "testuser_";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 여러 언더스코어 포함")
        void validate_MultipleUnderscores_Success() {
            // given
            String nickname = "test_user_123";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("유효하지 않은 닉네임 검증")
    class InvalidNicknames {

        @Test
        @DisplayName("실패 - 빈 문자열")
        void validate_EmptyString_Fail() {
            // given
            String nickname = "";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 너무 짧음 (2자)")
        void validate_TooShort_Fail() {
            // given
            String nickname = "ab";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 너무 김 (16자)")
        void validate_TooLong_Fail() {
            // given
            String nickname = "abcdefghijklmnop"; // 16자

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 공백 포함")
        void validate_WithSpaces_Fail() {
            // given
            String nickname = "test user";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 특수문자 포함 (하이픈)")
        void validate_WithHyphen_Fail() {
            // given
            String nickname = "test-user";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 특수문자 포함 (@)")
        void validate_WithAtSign_Fail() {
            // given
            String nickname = "test@user";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 특수문자 포함 (!)")
        void validate_WithExclamation_Fail() {
            // given
            String nickname = "test!";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 한글 포함")
        void validate_WithKorean_Fail() {
            // given
            String nickname = "테스트";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 한글과 영문 혼합")
        void validate_WithKoreanAndEnglish_Fail() {
            // given
            String nickname = "test테스트";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 이모지 포함")
        void validate_WithEmoji_Fail() {
            // given
            String nickname = "test😀";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 점(.) 포함")
        void validate_WithDot_Fail() {
            // given
            String nickname = "test.user";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("엣지 케이스")
    class EdgeCases {

        @Test
        @DisplayName("실패 - 단일 문자")
        void validate_SingleCharacter_Fail() {
            // given
            String nickname = "a";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 공백만")
        void validate_WhitespaceOnly_Fail() {
            // given
            String nickname = "   ";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("성공 - 숫자로 시작")
        void validate_StartWithNumber_Success() {
            // given
            String nickname = "123test";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 모든 언더스코어")
        void validate_AllUnderscores_Success() {
            // given
            String nickname = "___";

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패 - 매우 긴 닉네임 (100자)")
        void validate_VeryLongNickname_Fail() {
            // given
            String nickname = "a".repeat(100);

            // when
            boolean result = validator.isValid(nickname, context);

            // then
            assertThat(result).isFalse();
        }
    }
}
