package com.teambind.profileserver.utils;


import com.teambind.profileserver.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NickNameValidator {
    private final UserInfoRepository userInfoRepository;

    public boolean validateNickName(String nickname) {
        return userInfoRepository.existsByNickname(nickname);
    }
}
