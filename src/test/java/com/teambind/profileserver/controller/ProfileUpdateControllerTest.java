package com.teambind.profileserver.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import com.teambind.profileserver.utils.validator.ProfileUpdateValidator;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileUpdateController.class)
@DisplayName("ProfileUpdateController 테스트")
class ProfileUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileUpdateService profileUpdateService;

    @MockitoBean
    private ProfileUpdateValidator profileUpdateValidator;

    private UserInfo userInfo;
    private ProfileUpdateRequest validRequest;

    @BeforeEach
    void setUp() {
        userInfo = UserInfo.builder()
                .userId("testUser")
                .nickname("testNickname")
                .city("Seoul")
                .introduction("Hello")
                .sex('M')
                .build();

        validRequest = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .city("Busan")
                .introduction("New Introduction")
                .chattable(true)
                .publicProfile(true)
                .sex('F')
                .genres(Arrays.asList(1, 2))
                .instruments(Arrays.asList(1, 2))
                .build();
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 성공 (ver1)")
    void updateProfile_Success() throws Exception {
        // given
        String userId = "testUser";
        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateValidator).validateProfileUpdateRequest(
                validRequest.getNickname(),
                validRequest.getGenres(),
                validRequest.getInstruments()
        );
        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 검증 실패")
    void updateProfile_ValidationFail() throws Exception {
        // given
        String userId = "testUser";
        doThrow(new ProfileException(ErrorCode.NICKNAME_INVALID))
                .when(profileUpdateValidator).validateProfileUpdateRequest(
                        anyString(), anyList(), anyList()
                );

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(profileUpdateValidator).validateProfileUpdateRequest(
                validRequest.getNickname(),
                validRequest.getGenres(),
                validRequest.getInstruments()
        );
        verify(profileUpdateService, never()).updateProfile(anyString(), any());
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 닉네임 중복")
    void updateProfile_DuplicateNickname() throws Exception {
        // given
        String userId = "testUser";
        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenThrow(new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS));

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("프로필 전체 업데이트 - 성공 (ver2)")
    void updateProfileAll_Success() throws Exception {
        // given
        String userId = "testUser";
        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateValidator).validateProfileUpdateRequest(
                validRequest.getNickname(),
                validRequest.getGenres(),
                validRequest.getInstruments()
        );
        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("프로필 전체 업데이트 - 사용자 없음")
    void updateProfileAll_UserNotFound() throws Exception {
        // given
        String userId = "nonExistentUser";
        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenThrow(new ProfileException(ErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("프로필 부분 업데이트 - 최소한의 필드만 제공")
    void updateProfile_MinimalFields() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest minimalRequest = ProfileUpdateRequest.builder()
                .nickname("newNickname")
                .chattable(false)
                .publicProfile(false)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("프로필 전체 업데이트 - 모든 필드 제공")
    void updateProfileAll_AllFields() throws Exception {
        // given
        String userId = "testUser";
        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateValidator).validateProfileUpdateRequest(
                validRequest.getNickname(),
                validRequest.getGenres(),
                validRequest.getInstruments()
        );
        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }

    // ========== 연관관계 변경 테스트 ==========

    @Test
    @DisplayName("부분 업데이트 - 장르 일부 제거 (1,2,3 → 1,2)")
    void updateProfile_RemoveSomeGenres() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(Arrays.asList(1, 2)) // 기존 1,2,3에서 3 제거
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("부분 업데이트 - 악기 일부 추가 (1,2 → 1,2,3)")
    void updateProfile_AddSomeInstruments() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .instruments(Arrays.asList(1, 2, 3)) // 기존 1,2에 3 추가
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("부분 업데이트 - 장르 일부 교체 (1,2,3 → 1,2,4)")
    void updateProfile_ReplaceSomeGenres() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(Arrays.asList(1, 2, 4)) // 3 제거, 4 추가
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("부분 업데이트 - 빈 리스트로 전체 삭제 (1,2,3 → [])")
    void updateProfile_RemoveAllWithEmptyList() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(Arrays.asList()) // 전체 삭제
                .instruments(Arrays.asList()) // 전체 삭제
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("부분 업데이트 - null로 변경하지 않음")
    void updateProfile_NullNotChanged() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .genres(null) // null이므로 변경하지 않음
                .instruments(null) // null이므로 변경하지 않음
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfile(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver1", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfile(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("전체 업데이트 - 장르 전체 교체 (1,2,3 → 4,5)")
    void updateProfileAll_ReplaceAllGenres() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList(4, 5)) // 전체 교체
                .instruments(Arrays.asList(1, 2))
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("전체 업데이트 - 장르와 악기 동시 변경 (1,2 → 3,4)")
    void updateProfileAll_ReplaceGenresAndInstrumentsTogether() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList(3, 4)) // 전체 교체
                .instruments(Arrays.asList(3, 4)) // 전체 교체
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("전체 업데이트 - 모든 장르/악기 삭제")
    void updateProfileAll_RemoveAllGenresAndInstruments() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("testNickname")
                .genres(Arrays.asList()) // 전체 삭제
                .instruments(Arrays.asList()) // 전체 삭제
                .chattable(true)
                .publicProfile(true)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }

    @Test
    @DisplayName("전체 업데이트 - 복합 필드 변경 (닉네임 + 장르 + 악기 + 기타 정보)")
    void updateProfileAll_MultipleFieldsChange() throws Exception {
        // given
        String userId = "testUser";
        ProfileUpdateRequest request = ProfileUpdateRequest.builder()
                .nickname("changedNickname")
                .city("Incheon")
                .introduction("Updated introduction")
                .sex('F')
                .genres(Arrays.asList(2, 3, 4)) // 변경
                .instruments(Arrays.asList(1, 3)) // 변경
                .chattable(false)
                .publicProfile(false)
                .build();

        doNothing().when(profileUpdateValidator).validateProfileUpdateRequest(
                anyString(), anyList(), anyList()
        );
        when(profileUpdateService.updateProfileAll(eq(userId), any(ProfileUpdateRequest.class)))
                .thenReturn(userInfo);

        // when & then
        mockMvc.perform(put("/api/profiles/profiles/{userId}/ver2", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(profileUpdateService).updateProfileAll(eq(userId), any(ProfileUpdateRequest.class));
    }
}
