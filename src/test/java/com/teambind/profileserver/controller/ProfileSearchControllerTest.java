package com.teambind.profileserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.config.TestConfig;
import com.teambind.profileserver.dto.response.BatchUserSummaryResponse;
import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.service.search.ProfileSearchService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProfileSearchController 통합 테스트
 *
 * 테스트 전략:
 * 1. @SpringBootTest로 전체 Application Context 로드
 * 2. MockMvc를 활용한 HTTP 요청/응답 검증
 * 3. Service 계층은 MockBean으로 격리
 * 4. 다양한 검색 조건 테스트
 * 5. 커서 기반 페이징 검증
 * 6. 배치 조회 기능 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
@DisplayName("ProfileSearchController 통합 테스트")
class ProfileSearchControllerTest {

    private static final String BASE_URL = "/api/profiles/profiles";
    private static final String TEST_USER_ID = "testUser123";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ProfileSearchService profileSearchService;

    @Nested
    @DisplayName("GET /{userId} - 단일 프로필 조회")
    class GetProfileById {

        @Test
        @DisplayName("성공 - 유효한 사용자 ID로 프로필 조회")
        void getProfile_Success() throws Exception {
            // given
            UserResponse mockResponse = UserResponse.builder()
                    .userId(TEST_USER_ID)
                    .nickname("testNickname")
                    .city("서울")
                    .introduction("안녕하세요")
                    .sex('M')
                    .isChattable(true)
                    .isPublic(true)
                    .profileImageUrl("https://example.com/image.jpg")
                    .genres(List.of("Rock", "Jazz"))
                    .instruments(List.of("Guitar", "Piano"))
                    .build();

            when(profileSearchService.searchProfileById(eq(TEST_USER_ID)))
                    .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get(BASE_URL + "/{userId}", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.nickname").value("testNickname"))
                    .andExpect(jsonPath("$.city").value("서울"))
                    .andExpect(jsonPath("$.introduction").value("안녕하세요"))
                    .andExpect(jsonPath("$.sex").value("M"))
                    .andExpect(jsonPath("$.isChattable").value(true))
                    .andExpect(jsonPath("$.isPublic").value(true))
                    .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/image.jpg"))
                    .andExpect(jsonPath("$.genres").isArray())
                    .andExpect(jsonPath("$.genres[0]").value("Rock"))
                    .andExpect(jsonPath("$.instruments").isArray())
                    .andExpect(jsonPath("$.instruments[0]").value("Guitar"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자 ID")
        void getProfile_UserNotFound_Fail() throws Exception {
            // given
            when(profileSearchService.searchProfileById(anyString()))
                    .thenThrow(new ProfileException(ProfileErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get(BASE_URL + "/{userId}", "nonExistentUser"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("성공 - 최소 정보만 있는 프로필 조회")
        void getProfile_MinimalInfo_Success() throws Exception {
            // given
            UserResponse mockResponse = UserResponse.builder()
                    .userId(TEST_USER_ID)
                    .nickname("minimalUser")
                    .isChattable(false)
                    .isPublic(false)
                    .genres(List.of())
                    .instruments(List.of())
                    .build();

            when(profileSearchService.searchProfileById(eq(TEST_USER_ID)))
                    .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(get(BASE_URL + "/{userId}", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.nickname").value("minimalUser"))
                    .andExpect(jsonPath("$.city").doesNotExist())
                    .andExpect(jsonPath("$.genres").isEmpty())
                    .andExpect(jsonPath("$.instruments").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET / - 프로필 검색 (커서 기반 페이징)")
    class SearchProfiles {

        @Test
        @DisplayName("성공 - 검색 조건 없이 전체 조회")
        void searchProfiles_NoFilters_Success() throws Exception {
            // given
            UserResponse user1 = UserResponse.builder()
                    .userId("user1")
                    .nickname("user1Nick")
                    .isChattable(true)
                    .isPublic(true)
                    .genres(List.of())
                    .instruments(List.of())
                    .build();

            UserResponse user2 = UserResponse.builder()
                    .userId("user2")
                    .nickname("user2Nick")
                    .isChattable(true)
                    .isPublic(true)
                    .genres(List.of())
                    .instruments(List.of())
                    .build();

            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of(user1, user2));

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].userId").value("user1"))
                    .andExpect(jsonPath("$.content[1].userId").value("user2"));
        }

        @Test
        @DisplayName("성공 - 도시로 검색")
        void searchProfiles_ByCity_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("city", "서울"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 닉네임으로 검색")
        void searchProfiles_ByNickname_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("nickName", "testNick"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 장르로 검색")
        void searchProfiles_ByGenres_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("genres", "1", "2", "3"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 악기로 검색")
        void searchProfiles_ByInstruments_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("instruments", "1", "2"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 성별로 검색")
        void searchProfiles_BySex_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("sex", "M"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 복합 조건 검색 (도시 + 장르 + 악기)")
        void searchProfiles_MultipleFilters_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("city", "서울")
                            .param("genres", "1", "2")
                            .param("instruments", "1")
                            .param("sex", "F"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 커서와 사이즈 지정")
        void searchProfiles_WithCursorAndSize_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), eq("cursor123"), eq(20)))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("cursor", "cursor123")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 사이즈 기본값 적용 (파라미터 없음)")
        void searchProfiles_DefaultSize_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), eq(10)))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 사이즈 최대값 제한 (100 초과 시 100으로 제한)")
        void searchProfiles_MaxSizeLimit_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), eq(100)))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("size", "150"))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 빈 문자열 파라미터는 null 처리")
        void searchProfiles_BlankParams_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("city", "")
                            .param("nickName", "   ")
                            .param("cursor", ""))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("성공 - 빈 결과 반환")
        void searchProfiles_EmptyResult_Success() throws Exception {
            // given
            Slice<UserResponse> mockSlice = new SliceImpl<>(List.of());

            when(profileSearchService.searchProfilesByCursor(any(ProfileSearchCriteria.class), any(), anyInt()))
                    .thenReturn(mockSlice);

            // when & then
            mockMvc.perform(get(BASE_URL)
                            .param("city", "존재하지않는도시"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /batch - 배치 프로필 조회")
    class GetProfilesBatch {

        @Test
        @DisplayName("성공 - 여러 사용자 ID로 배치 조회")
        void getProfilesBatch_Success() throws Exception {
            // given
            List<String> userIds = List.of("user1", "user2", "user3");

            BatchUserSummaryResponse response1 = BatchUserSummaryResponse.builder()
                    .userId("user1")
                    .nickname("nick1")
                    .profileImageUrl("https://example.com/1.jpg")
                    .build();

            BatchUserSummaryResponse response2 = BatchUserSummaryResponse.builder()
                    .userId("user2")
                    .nickname("nick2")
                    .profileImageUrl("https://example.com/2.jpg")
                    .build();

            BatchUserSummaryResponse response3 = BatchUserSummaryResponse.builder()
                    .userId("user3")
                    .nickname("nick3")
                    .profileImageUrl("https://example.com/3.jpg")
                    .build();

            List<BatchUserSummaryResponse> mockResponses = List.of(response1, response2, response3);

            when(profileSearchService.searchProfilesByIds(eq(userIds)))
                    .thenReturn(mockResponses);

            // when & then
            mockMvc.perform(post(BASE_URL + "/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].userId").value("user1"))
                    .andExpect(jsonPath("$[0].nickname").value("nick1"))
                    .andExpect(jsonPath("$[1].userId").value("user2"))
                    .andExpect(jsonPath("$[2].userId").value("user3"));
        }

        @Test
        @DisplayName("성공 - 단일 사용자 ID로 배치 조회")
        void getProfilesBatch_SingleUser_Success() throws Exception {
            // given
            List<String> userIds = List.of("user1");

            BatchUserSummaryResponse response = BatchUserSummaryResponse.builder()
                    .userId("user1")
                    .nickname("nick1")
                    .profileImageUrl("https://example.com/1.jpg")
                    .build();

            when(profileSearchService.searchProfilesByIds(eq(userIds)))
                    .thenReturn(List.of(response));

            // when & then
            mockMvc.perform(post(BASE_URL + "/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].userId").value("user1"));
        }

        @Test
        @DisplayName("성공 - 빈 리스트로 배치 조회")
        void getProfilesBatch_EmptyList_Success() throws Exception {
            // given
            List<String> userIds = List.of();

            when(profileSearchService.searchProfilesByIds(eq(userIds)))
                    .thenReturn(List.of());

            // when & then
            mockMvc.perform(post(BASE_URL + "/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("실패 - 잘못된 JSON 형식")
        void getProfilesBatch_InvalidJson_Fail() throws Exception {
            // given
            String invalidJson = "{ invalid json }";

            // when & then
            mockMvc.perform(post(BASE_URL + "/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - Content-Type 누락")
        void getProfilesBatch_NoContentType_Fail() throws Exception {
            // given
            List<String> userIds = List.of("user1");

            // when & then
            mockMvc.perform(post(BASE_URL + "/batch")
                            .content(objectMapper.writeValueAsString(userIds)))
                    .andDo(print())
                    .andExpect(status().isUnsupportedMediaType());
        }
    }
}
