package com.teambind.profileserver.service.utils;

import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.utils.validator.ProfileUpdateValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NickNameValidatorServiceServiceTest {

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private ProfileUpdateValidator profileUpdateValidator;

    @InjectMocks
    private NickNameValidatorService nickNameValidatorService;

    @Test
    @DisplayName("닉네임 유효하고 존재하지 않는 경우: true 반환")
    void validateNickname_valid_returnsTrue() {
        String nickname = "validNick";
        when(profileUpdateValidator.NicknameValidation(nickname)).thenReturn(false);
        when(userInfoRepository.existsByNickname(nickname)).thenReturn(false);

        boolean result = nickNameValidatorService.validateNickname(nickname);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("닉네임 정규식 등 형식 위반: NICKNAME_INVALID 예외")
    void validateNickname_invalidFormat_throwsException() {
        String nickname = "__"; // invalid per validator indicating true -> invalid
        when(profileUpdateValidator.NicknameValidation(nickname)).thenReturn(true);

        ProfileException ex = assertThrows(ProfileException.class, () -> nickNameValidatorService.validateNickname(nickname));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_INVALID);
    }

    @Test
    @DisplayName("닉네임 이미 존재: NICKNAME_ALREADY_EXISTS 예외")
    void validateNickname_alreadyExists_throwsException() {
        String nickname = "takenNick";
        when(profileUpdateValidator.NicknameValidation(nickname)).thenReturn(false);
        when(userInfoRepository.existsByNickname(nickname)).thenReturn(true);

        ProfileException ex = assertThrows(ProfileException.class, () -> nickNameValidatorService.validateNickname(nickname));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }
}
