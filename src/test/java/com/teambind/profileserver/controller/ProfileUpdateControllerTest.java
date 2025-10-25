package com.teambind.profileserver.controller;

import static com.teambind.profileserver.fixture.TestFixtureFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileUpdateController 통합 테스트
 *
 * <p>테스트 전략: 1. @SpringBootTest로 전체 Application Context 로드 (InitTableMapper 포함) 2. MockMvc를 활용한
 * HTTP 요청/응답 검증 3. Service 계층은 MockBean으로 격리 4. JSON 직렬화/역직렬화 검증 5. 검증 어노테이션 동작 확인
 * (AttributeValidator가 InitTableMapper 참조) 6. H2 in-memory DB 사용 (test 프로필) 7. TestConfig로 Kafka
 * Mock Bean 제공
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("ProfileUpdateController 통합 테스트")
class ProfileUpdateControllerTest {

  private static final String BASE_URL = "/api/profiles/profiles";
  private static final String TEST_USER_ID = "testUser123";
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private ProfileUpdateService profileUpdateService;
  @Autowired private com.teambind.profileserver.utils.InitTableMapper initTableMapper;

  @BeforeEach
  void setUp() {
    // InitTableMapper를 수동으로 초기화하여 Validator가 사용할 수 있도록 함
    initTableMapper.initializeTables();

    // 테스트용 지역 데이터 추가
    com.teambind.profileserver.utils.InitTableMapper.locationNamesTable.put("SEOUL", "서울");
    com.teambind.profileserver.utils.InitTableMapper.locationNamesTable.put("BUSAN", "부산");
    com.teambind.profileserver.utils.InitTableMapper.locationNamesTable.put("DAEGU", "대구");
    com.teambind.profileserver.utils.InitTableMapper.locationNamesTable.put("INCHEON", "인천");

    // Service는 기본적으로 정상 동작한다고 가정
    doNothing().when(profileUpdateService).updateProfile(any(), any());
  }

  @Nested
  @DisplayName("PUT /{userId} - 프로필 업데이트")
  class UpdateProfile {

    @Test
    @DisplayName("성공 - 닉네임만 변경")
    void updateProfile_OnlyNickname_Success() throws Exception {
      // given
      ProfileUpdateRequest request = updateRequest().nickname("newNickname").build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").value(true));

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("성공 - 모든 필드 변경 (장르/악기 제외)")
    void updateProfile_AllFields_Success() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("completeNick")
              .city("SEOUL")
              .introduction("자기소개")
              .sex('M')
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(true));

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("성공 - 최소 필드만 제공")
    void updateProfile_MinimalFields_Success() throws Exception {
      // given - nickname은 필수이므로 유효한 값 제공
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .chattable(false)
              .publicProfile(false)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk());

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("실패 - 닉네임 중복")
    void updateProfile_DuplicateNickname_Fail() throws Exception {
      // given
      ProfileUpdateRequest request = updateRequest().nickname("duplicateNick").build();

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
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("실패 - 사용자 없음")
    void updateProfile_UserNotFound_Fail() throws Exception {
      // given
      ProfileUpdateRequest request = createBasicUpdateRequest();

      doThrow(new ProfileException(ProfileErrorCode.USER_NOT_FOUND))
          .when(profileUpdateService)
          .updateProfile(any(), any());

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", "nonExistentUser")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("실패 - 잘못된 JSON 형식")
    void updateProfile_InvalidJson_Fail() throws Exception {
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

      verify(profileUpdateService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("실패 - Content-Type 누락")
    void updateProfile_NoContentType_Fail() throws Exception {
      // given
      ProfileUpdateRequest request = createBasicUpdateRequest();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .content(objectMapper.writeValueAsString(request))) // Content-Type 없음
          .andDo(print())
          .andExpect(status().isUnsupportedMediaType());

      verify(profileUpdateService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("성공 - 유효한 지역(city)으로 업데이트")
    void updateProfile_ValidCity_Success() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city("SEOUL") // 유효한 지역
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(true));

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("실패 - 잘못된 지역(city)으로 업데이트")
    void updateProfile_InvalidCity_Fail() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city("INVALID_CITY") // 잘못된 지역
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isBadRequest());

      verify(profileUpdateService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("성공 - 지역(city) 변경")
    void updateProfile_ChangeCity_Success() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city("BUSAN") // 부산으로 변경
              .introduction("부산 사는 뮤지션입니다")
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(true));

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("성공 - 지역(city) null (변경하지 않음)")
    void updateProfile_NullCity_Success() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("testuser")
              .city(null) // null은 변경하지 않음
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(true));

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("성공 - 모든 필드 변경 (지역 포함)")
    void updateProfile_AllFieldsWithCity_Success() throws Exception {
      // given
      ProfileUpdateRequest request =
          ProfileUpdateRequest.builder()
              .nickname("completeUser")
              .city("DAEGU") // 대구
              .introduction("대구 뮤지션")
              .sex('F')
              .chattable(true)
              .publicProfile(true)
              .build();

      // when & then
      mockMvc
          .perform(
              put(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(true));

      verify(profileUpdateService).updateProfile(eq(TEST_USER_ID), any(ProfileUpdateRequest.class));
    }
  }

  @Nested
  @DisplayName("HTTP 메서드 테스트")
  class HttpMethodTests {

    @Test
    @DisplayName("잘못된 HTTP 메서드 - POST")
    void wrongHttpMethod_Post() throws Exception {
      // when & then
      mockMvc
          .perform(
              post(BASE_URL + "/{userId}", TEST_USER_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createBasicUpdateRequest())))
          .andDo(print())
          .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("잘못된 HTTP 메서드 - DELETE")
    void wrongHttpMethod_Delete() throws Exception {
      // when & then
      mockMvc
          .perform(
              org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                  BASE_URL + "/{userId}", TEST_USER_ID))
          .andDo(print())
          .andExpect(status().isMethodNotAllowed());
    }
  }
}
