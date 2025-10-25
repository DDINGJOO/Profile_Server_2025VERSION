package com.teambind.profileserver.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.teambind.profileserver.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * HealthCheckController 통합 테스트
 *
 * <p>테스트 전략: 1. @SpringBootTest로 전체 Application Context 로드 2. MockMvc를 활용한 HTTP 요청/응답 검증 3. 서버 상태
 * 확인 엔드포인트 검증 4. HTTP 메서드 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("HealthCheckController 통합 테스트")
class HealthCheckControllerTest {

  private static final String HEALTH_URL = "/health";
  @Autowired private MockMvc mockMvc;

  @Nested
  @DisplayName("GET /health - 서버 상태 확인")
  class HealthCheck {

    @Test
    @DisplayName("성공 - 서버 상태 확인")
    void healthCheck_Success() throws Exception {
      // when & then
      mockMvc
          .perform(get(HEALTH_URL))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(content().contentType("text/plain;charset=UTF-8"))
          .andExpect(content().string("Server is Running"));
    }

    @Test
    @DisplayName("성공 - 응답이 String 타입")
    void healthCheck_ResponseType_Success() throws Exception {
      // when & then
      mockMvc
          .perform(get(HEALTH_URL))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    @DisplayName("성공 - 여러 번 호출해도 동일한 결과 반환")
    void healthCheck_Idempotent_Success() throws Exception {
      // when & then - 첫 번째 호출
      mockMvc
          .perform(get(HEALTH_URL))
          .andExpect(status().isOk())
          .andExpect(content().string("Server is Running"));

      // when & then - 두 번째 호출
      mockMvc
          .perform(get(HEALTH_URL))
          .andExpect(status().isOk())
          .andExpect(content().string("Server is Running"));

      // when & then - 세 번째 호출
      mockMvc
          .perform(get(HEALTH_URL))
          .andExpect(status().isOk())
          .andExpect(content().string("Server is Running"));
    }

    @Test
    @DisplayName("실패 - 잘못된 HTTP 메서드 (POST)")
    void healthCheck_WrongMethod_Post_Fail() throws Exception {
      // when & then
      mockMvc.perform(post(HEALTH_URL)).andDo(print()).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("실패 - 잘못된 HTTP 메서드 (PUT)")
    void healthCheck_WrongMethod_Put_Fail() throws Exception {
      // when & then
      mockMvc.perform(put(HEALTH_URL)).andDo(print()).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("실패 - 잘못된 HTTP 메서드 (DELETE)")
    void healthCheck_WrongMethod_Delete_Fail() throws Exception {
      // when & then
      mockMvc.perform(delete(HEALTH_URL)).andDo(print()).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("실패 - 잘못된 HTTP 메서드 (PATCH)")
    void healthCheck_WrongMethod_Patch_Fail() throws Exception {
      // when & then
      mockMvc.perform(patch(HEALTH_URL)).andDo(print()).andExpect(status().isMethodNotAllowed());
    }
  }

  @Nested
  @DisplayName("경로 테스트")
  class PathTests {

    @Test
    @DisplayName("실패 - 잘못된 경로 (/health/check)")
    void healthCheck_WrongPath_Fail() throws Exception {
      // when & then
      mockMvc.perform(get("/health/check")).andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("실패 - 잘못된 경로 (/healthcheck)")
    void healthCheck_WrongPath_NoSlash_Fail() throws Exception {
      // when & then
      mockMvc.perform(get("/healthcheck")).andDo(print()).andExpect(status().isNotFound());
    }
  }
}
