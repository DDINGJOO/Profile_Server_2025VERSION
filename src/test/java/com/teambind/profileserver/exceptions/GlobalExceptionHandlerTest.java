package com.teambind.profileserver.exceptions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * GlobalExceptionHandler 통합 테스트
 *
 * <p>테스트 전략: 1. @SpringBootTest로 전체 Application Context 로드 2. MockMvc를 활용한 예외 처리 검증 3.
 * ProfileException 처리 검증 4. MethodArgumentNotValidException 처리 검증 5. ErrorResponse 구조 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("GlobalExceptionHandler 통합 테스트")
class GlobalExceptionHandlerTest {

  private static final String BASE_URL = "/api/profiles/profiles";
  private static final String TEST_USER_ID = "testUser123";
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private ProfileUpdateService profileUpdateService;

  @Nested
  @DisplayName("ProfileException 처리")
  class ProfileExceptionHandling {

    @Test
    @DisplayName("성공 - USER_NOT_FOUND 예외 처리 (404)")
    void handleProfileException_UserNotFound_Returns404() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.USER_NOT_FOUND))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status").value(404))
          .andExpect(jsonPath("$.code").value("PROFILE_007"))
          .andExpect(jsonPath("$.message").value("User not found"))
          .andExpect(jsonPath("$.path").value(BASE_URL + "/" + TEST_USER_ID))
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("성공 - NICKNAME_ALREADY_EXISTS 예외 처리 (409)")
    void handleProfileException_NicknameAlreadyExists_Returns409() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("duplicate")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.NICKNAME_ALREADY_EXISTS))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isConflict())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status").value(409))
          .andExpect(jsonPath("$.code").value("PROFILE_001"))
          .andExpect(jsonPath("$.message").value("Nickname already exists"))
          .andExpect(jsonPath("$.path").value(BASE_URL + "/" + TEST_USER_ID))
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("성공 - GENRE_SIZE_INVALID 예외 처리 (400)")
    void handleProfileException_GenreSizeInvalid_Returns400() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.GENRE_SIZE_INVALID))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.code").value("PROFILE_003"))
          .andExpect(jsonPath("$.message").value("Genre size not valid"))
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("성공 - INSTRUMENT_SIZE_INVALID 예외 처리 (400)")
    void handleProfileException_InstrumentSizeInvalid_Returns400() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.INSTRUMENT_SIZE_INVALID))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.code").value("PROFILE_005"))
          .andExpect(jsonPath("$.message").value("Instrument size not valid"));
    }

    @Test
    @DisplayName("성공 - NICKNAME_INVALID 예외 처리 (400)")
    void handleProfileException_NicknameInvalid_Returns400() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.NICKNAME_INVALID))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.code").value("PROFILE_008"))
          .andExpect(jsonPath("$.message").value("Nickname is invalid"));
    }

    @Test
    @DisplayName("성공 - HISTORY_UPDATE_FAILED 예외 처리 (500)")
    void handleProfileException_HistoryUpdateFailed_Returns500() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.HISTORY_UPDATE_FAILED))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.status").value(500))
          .andExpect(jsonPath("$.code").value("PROFILE_002"))
          .andExpect(jsonPath("$.message").value("Failed to update history"));
    }
  }

  @Nested
  @DisplayName("MethodArgumentNotValidException 처리")
  class ValidationExceptionHandling {

    @Test
    @DisplayName("성공 - 잘못된 JSON 형식 (400)")
    void handleValidationException_InvalidJson_Returns400() throws Exception {
      // given
      String invalidJson = "{ invalid json }";

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidJson))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("성공 - 필드 타입 불일치")
    void handleValidationException_TypeMismatch_Returns400() throws Exception {
      // given - chattable은 boolean인데 문자열 제공
      String invalidTypeJson =
          """
                {
                    "nickname": "testuser",
                    "chattable": "not_a_boolean",
                    "publicProfile": false
                }
                """;

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(invalidTypeJson))
          .andDo(print())
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("ErrorResponse 구조 검증")
  class ErrorResponseStructure {

    @Test
    @DisplayName("성공 - ErrorResponse에 모든 필수 필드 포함")
    void errorResponse_HasAllRequiredFields() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.USER_NOT_FOUND))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.timestamp").exists())
          .andExpect(jsonPath("$.status").exists())
          .andExpect(jsonPath("$.code").exists())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("성공 - timestamp 형식 검증 (ISO-8601)")
    void errorResponse_TimestampFormat_IsValid() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.USER_NOT_FOUND))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.timestamp").isString())
          // ISO-8601 형식: yyyy-MM-dd'T'HH:mm:ss
          .andExpect(
              jsonPath("$.timestamp")
                  .value(
                      org.hamcrest.Matchers.matchesRegex(
                          "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")));
    }

    @Test
    @DisplayName("성공 - path에 요청 URI 포함")
    void errorResponse_PathContainsRequestUri() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      doThrow(new ProfileException(ProfileErrorCode.NICKNAME_ALREADY_EXISTS))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      String requestUri = BASE_URL + "/" + TEST_USER_ID;

      // when & then
      mockMvc
          .perform(
              put(requestUri)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.path").value(requestUri));
    }
  }
}
