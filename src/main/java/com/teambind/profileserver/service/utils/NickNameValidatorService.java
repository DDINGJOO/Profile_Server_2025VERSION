package com.teambind.profileserver.service.utils;


import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NickNameValidatorService {

    private final UserInfoRepository userInfoRepository;
    private final ProfileUpdateValidator validator;

    public boolean validateNickname(String nickname) {
        if(validator.NicknameValidation(nickname)) {
            throw new ProfileException(ErrorCode.NICKNAME_INVALID);
        }
        if(userInfoRepository.existsByNickname(nickname)) {
            throw new ProfileException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        return true;
    }

}
