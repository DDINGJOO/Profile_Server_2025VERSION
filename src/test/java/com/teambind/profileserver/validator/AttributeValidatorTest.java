package com.teambind.profileserver.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.utils.InitTableMapper;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AttributeValidator 단위 테스트
 *
 * 테스트 전략:
 * 1. Mockito를 활용한 단위 테스트
 * 2. InitTableMapper static 필드를 직접 설정
 * 3. 장르 및 악기 ID 검증 로직 검증
 * 4. 최대 크기 제한 검증
 * 5. 예외 상황 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttributeValidator 단위 테스트")
class AttributeValidatorTest {

    private AttributeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AttributeValidator();
        ReflectionTestUtils.setField(validator, "maxSize", 3);

        // InitTableMapper의 static 필드 초기화
        setupInitTableMapper();
    }

    private void setupInitTableMapper() {
        // Genre 테스트 데이터
        HashMap<Integer, GenreNameTable> genreMap = new HashMap<>();
        genreMap.put(1, GenreNameTable.builder().genreId(1).genreName("Rock").build());
        genreMap.put(2, GenreNameTable.builder().genreId(2).genreName("Jazz").build());
        genreMap.put(3, GenreNameTable.builder().genreId(3).genreName("Classical").build());
        genreMap.put(4, GenreNameTable.builder().genreId(4).genreName("Pop").build());
        genreMap.put(5, GenreNameTable.builder().genreId(5).genreName("Hip-Hop").build());

        // Instrument 테스트 데이터
        HashMap<Integer, InstrumentNameTable> instrumentMap = new HashMap<>();
        instrumentMap.put(1, InstrumentNameTable.builder().instrumentId(1).instrumentName("Guitar").build());
        instrumentMap.put(2, InstrumentNameTable.builder().instrumentId(2).instrumentName("Piano").build());
        instrumentMap.put(3, InstrumentNameTable.builder().instrumentId(3).instrumentName("Drum").build());
        instrumentMap.put(4, InstrumentNameTable.builder().instrumentId(4).instrumentName("Bass").build());
        instrumentMap.put(5, InstrumentNameTable.builder().instrumentId(5).instrumentName("Violin").build());

        // Static 필드에 직접 할당
        InitTableMapper.genreNameTable = genreMap;
        InitTableMapper.instrumentNameTable = instrumentMap;
    }

    @Nested
    @DisplayName("장르(GENRE) 검증")
    class GenreValidation {

        @BeforeEach
        void setUp() {
            Attribute annotation = mock(Attribute.class);
            org.mockito.Mockito.when(annotation.value()).thenReturn("GENRE");
            validator.initialize(annotation);
        }

        @Test
        @DisplayName("성공 - null 값")
        void validate_NullValue_Success() {
            // when
            boolean result = validator.isValid(null, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 빈 리스트")
        void validate_EmptyList_Success() {
            // given
            List<Integer> genreIds = List.of();

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 유효한 장르 ID 1개")
        void validate_SingleValidId_Success() {
            // given
            List<Integer> genreIds = List.of(1);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 유효한 장르 ID 여러 개")
        void validate_MultipleValidIds_Success() {
            // given
            List<Integer> genreIds = List.of(1, 2, 3);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 최대 개수 (3개)")
        void validate_MaxSize_Success() {
            // given
            List<Integer> genreIds = List.of(1, 2, 3);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패 - 최대 개수 초과 (4개)")
        void validate_ExceedsMaxSize_Fail() {
            // given
            List<Integer> genreIds = List.of(1, 2, 3, 4);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 장르 ID")
        void validate_InvalidGenreId_ThrowsException() {
            // given
            List<Integer> genreIds = List.of(999);

            // when & then
            assertThatThrownBy(() -> validator.isValid(genreIds, context))
                    .isInstanceOf(ProfileException.class);
        }

        @Test
        @DisplayName("예외 - 유효한 ID와 무효한 ID 혼합")
        void validate_MixedValidAndInvalidIds_ThrowsException() {
            // given
            List<Integer> genreIds = List.of(1, 2, 999);

            // when & then
            assertThatThrownBy(() -> validator.isValid(genreIds, context))
                    .isInstanceOf(ProfileException.class);
        }

        @Test
        @DisplayName("성공 - 중복된 ID (검증 통과)")
        void validate_DuplicateIds_Success() {
            // given
            List<Integer> genreIds = List.of(1, 1, 2);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("예외 - 음수 ID")
        void validate_NegativeId_ThrowsException() {
            // given
            List<Integer> genreIds = List.of(-1);

            // when & then
            assertThatThrownBy(() -> validator.isValid(genreIds, context))
                    .isInstanceOf(ProfileException.class);
        }

        @Test
        @DisplayName("예외 - 0 ID")
        void validate_ZeroId_ThrowsException() {
            // given
            List<Integer> genreIds = List.of(0);

            // when & then
            assertThatThrownBy(() -> validator.isValid(genreIds, context))
                    .isInstanceOf(ProfileException.class);
        }
    }

    @Nested
    @DisplayName("악기(INTEREST) 검증")
    class InterestValidation {

        @BeforeEach
        void setUp() {
            Attribute annotation = mock(Attribute.class);
            org.mockito.Mockito.when(annotation.value()).thenReturn("INTEREST");
            validator.initialize(annotation);
        }

        @Test
        @DisplayName("성공 - null 값")
        void validate_NullValue_Success() {
            // when
            boolean result = validator.isValid(null, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 빈 리스트")
        void validate_EmptyList_Success() {
            // given
            List<Integer> instrumentIds = List.of();

            // when
            boolean result = validator.isValid(instrumentIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 유효한 악기 ID 1개")
        void validate_SingleValidId_Success() {
            // given
            List<Integer> instrumentIds = List.of(1);

            // when
            boolean result = validator.isValid(instrumentIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 유효한 악기 ID 여러 개")
        void validate_MultipleValidIds_Success() {
            // given
            List<Integer> instrumentIds = List.of(1, 2, 3);

            // when
            boolean result = validator.isValid(instrumentIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 최대 개수 (3개)")
        void validate_MaxSize_Success() {
            // given
            List<Integer> instrumentIds = List.of(1, 2, 3);

            // when
            boolean result = validator.isValid(instrumentIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패 - 최대 개수 초과 (4개)")
        void validate_ExceedsMaxSize_Fail() {
            // given
            List<Integer> instrumentIds = List.of(1, 2, 3, 4);

            // when
            boolean result = validator.isValid(instrumentIds, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("예외 - 존재하지 않는 악기 ID")
        void validate_InvalidInstrumentId_ThrowsException() {
            // given
            List<Integer> instrumentIds = List.of(999);

            // when & then
            assertThatThrownBy(() -> validator.isValid(instrumentIds, context))
                    .isInstanceOf(ProfileException.class);
        }

        @Test
        @DisplayName("예외 - 유효한 ID와 무효한 ID 혼합")
        void validate_MixedValidAndInvalidIds_ThrowsException() {
            // given
            List<Integer> instrumentIds = List.of(1, 2, 999);

            // when & then
            assertThatThrownBy(() -> validator.isValid(instrumentIds, context))
                    .isInstanceOf(ProfileException.class);
        }

        @Test
        @DisplayName("성공 - 중복된 ID (검증 통과)")
        void validate_DuplicateIds_Success() {
            // given
            List<Integer> instrumentIds = List.of(1, 1, 2);

            // when
            boolean result = validator.isValid(instrumentIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("예외 - 음수 ID")
        void validate_NegativeId_ThrowsException() {
            // given
            List<Integer> instrumentIds = List.of(-1);

            // when & then
            assertThatThrownBy(() -> validator.isValid(instrumentIds, context))
                    .isInstanceOf(ProfileException.class);
        }

        @Test
        @DisplayName("예외 - 0 ID")
        void validate_ZeroId_ThrowsException() {
            // given
            List<Integer> instrumentIds = List.of(0);

            // when & then
            assertThatThrownBy(() -> validator.isValid(instrumentIds, context))
                    .isInstanceOf(ProfileException.class);
        }
    }

    @Nested
    @DisplayName("최대 크기 설정 테스트")
    class MaxSizeConfiguration {

        @Test
        @DisplayName("성공 - maxSize 변경 테스트 (5개)")
        void validate_CustomMaxSize_Success() {
            // given
            Attribute annotation = mock(Attribute.class);
            org.mockito.Mockito.when(annotation.value()).thenReturn("GENRE");
            validator.initialize(annotation);

            ReflectionTestUtils.setField(validator, "maxSize", 5);
            List<Integer> genreIds = List.of(1, 2, 3, 4, 5);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패 - maxSize 초과 (6개)")
        void validate_ExceedsCustomMaxSize_Fail() {
            // given
            Attribute annotation = mock(Attribute.class);
            org.mockito.Mockito.when(annotation.value()).thenReturn("GENRE");
            validator.initialize(annotation);

            ReflectionTestUtils.setField(validator, "maxSize", 5);
            List<Integer> genreIds = List.of(1, 2, 3, 4, 5, 1);

            // when
            boolean result = validator.isValid(genreIds, context);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("잘못된 플래그 테스트")
    class InvalidFlagTest {

        @Test
        @DisplayName("실패 - 잘못된 플래그 (UNKNOWN)")
        void validate_InvalidFlag_Fail() {
            // given
            Attribute annotation = mock(Attribute.class);
            org.mockito.Mockito.when(annotation.value()).thenReturn("UNKNOWN");
            validator.initialize(annotation);

            List<Integer> ids = List.of(1, 2, 3);

            // when
            boolean result = validator.isValid(ids, context);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - 빈 플래그")
        void validate_EmptyFlag_Fail() {
            // given
            Attribute annotation = mock(Attribute.class);
            org.mockito.Mockito.when(annotation.value()).thenReturn("");
            validator.initialize(annotation);

            List<Integer> ids = List.of(1, 2, 3);

            // when
            boolean result = validator.isValid(ids, context);

            // then
            assertThat(result).isFalse();
        }
    }
}
