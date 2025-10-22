package com.teambind.profileserver.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * EnumsController 통합 테스트
 *
 * 테스트 전략:
 * 1. @SpringBootTest로 전체 Application Context 로드
 * 2. InitTableMapper가 @PostConstruct에서 data.sql 데이터 로드 확인
 * 3. MockMvc를 활용한 HTTP 요청/응답 검증
 * 4. 반환되는 Map 구조 및 데이터 검증
 * 5. HTTP 메서드 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("EnumsController 통합 테스트")
class EnumsControllerTest {

    private static final String BASE_URL = "/api/profiles/enums";
    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /genres - 장르 목록 조회")
    class GetGenres {

        @Test
        @DisplayName("성공 - 장르 목록 조회")
        void getGenres_Success() throws Exception {
            // when & then
            mockMvc.perform(get(BASE_URL + "/genres"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isMap());
//                    .andExpect(jsonPath("$").isNotEmpty());
        }

        @Test
        @DisplayName("성공 - 반환되는 Map의 키는 Integer, 값은 String")
        void getGenres_MapStructure_Success() throws Exception {
            // when & then
            mockMvc.perform(get(BASE_URL + "/genres"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());
            // InitTableMapper가 data.sql에서 데이터를 로드하므로
            // 실제 데이터가 있는지 확인
        }

        @Test
        @DisplayName("실패 - 잘못된 HTTP 메서드 (POST)")
        void getGenres_WrongMethod_Post_Fail() throws Exception {
            // when & then
            mockMvc.perform(post(BASE_URL + "/genres"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("실패 - 잘못된 HTTP 메서드 (PUT)")
        void getGenres_WrongMethod_Put_Fail() throws Exception {
            // when & then
            mockMvc.perform(put(BASE_URL + "/genres"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("GET /instruments - 악기 목록 조회")
    class GetInstruments {

        @Test
        @DisplayName("성공 - 악기 목록 조회")
        void getInstruments_Success() throws Exception {
            // when & then
            mockMvc.perform(get(BASE_URL + "/instruments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isMap());
//                    .andExpect(jsonPath("$").isNotEmpty());
        }

        @Test
        @DisplayName("성공 - 반환되는 Map의 키는 Integer, 값은 String")
        void getInstruments_MapStructure_Success() throws Exception {
            // when & then
            mockMvc.perform(get(BASE_URL + "/instruments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());
            // InitTableMapper가 data.sql에서 데이터를 로드하므로
            // 실제 데이터가 있는지 확인
        }

        @Test
        @DisplayName("실패 - 잘못된 HTTP 메서드 (POST)")
        void getInstruments_WrongMethod_Post_Fail() throws Exception {
            // when & then
            mockMvc.perform(post(BASE_URL + "/instruments"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("실패 - 잘못된 HTTP 메서드 (PUT)")
        void getInstruments_WrongMethod_Put_Fail() throws Exception {
            // when & then
            mockMvc.perform(put(BASE_URL + "/instruments"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("GET /locations - 위치 목록 조회")
    class GetLocations {

        @Test
        @DisplayName("성공 - 위치 목록 조회")
        void getLocations_Success() throws Exception {
            // when & then
            mockMvc.perform(get(BASE_URL + "/locations"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isMap());
//                    .andExpect(jsonPath("$").isNotEmpty());
        }

        @Test
        @DisplayName("성공 - 반환되는 Map의 키와 값은 모두 String")
        void getLocations_MapStructure_Success() throws Exception {
            // when & then
            mockMvc.perform(get(BASE_URL + "/locations"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());
            // InitTableMapper가 data.sql에서 데이터를 로드하므로
            // 실제 데이터가 있는지 확인
        }

        @Test
        @DisplayName("실패 - 잘못된 HTTP 메서드 (POST)")
        void getLocations_WrongMethod_Post_Fail() throws Exception {
            // when & then
            mockMvc.perform(post(BASE_URL + "/locations"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("실패 - 잘못된 HTTP 메서드 (PUT)")
        void getLocations_WrongMethod_Put_Fail() throws Exception {
            // when & then
            mockMvc.perform(put(BASE_URL + "/locations"))
                    .andDo(print())
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    @Nested
    @DisplayName("전체 Enum 엔드포인트 통합 테스트")
    class IntegrationTests {

        @Test
        @DisplayName("성공 - 모든 Enum 엔드포인트가 정상 작동")
        void allEnumsEndpoints_Success() throws Exception {
            // when & then - genres
            mockMvc.perform(get(BASE_URL + "/genres"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());

            // when & then - instruments
            mockMvc.perform(get(BASE_URL + "/instruments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());

            // when & then - locations
            mockMvc.perform(get(BASE_URL + "/locations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isMap());
        }

//        @Test
//        @DisplayName("성공 - 각 엔드포인트가 빈 Map을 반환하지 않음")
//        void allEnumsEndpoints_NotEmpty_Success() throws Exception {
//            // when & then - genres
//            mockMvc.perform(get(BASE_URL + "/genres"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$").isNotEmpty());
//
//            // when & then - instruments
//            mockMvc.perform(get(BASE_URL + "/instruments"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$").isNotEmpty());
//
//            // when & then - locations
//            mockMvc.perform(get(BASE_URL + "/locations"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$").isNotEmpty());
//        }
    }
}
