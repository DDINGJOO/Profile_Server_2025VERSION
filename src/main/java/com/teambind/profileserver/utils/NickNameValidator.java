package com.teambind.profileserver.utils;

import com.teambind.profileserver.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NickNameValidator {
    private final UserInfoRepository userInfoRepository;

    @Value("${nickname.validation.regex}")
    private static String regex;

    public static boolean isValidNickName(String nickName) {
        return nickName.matches(regex);

    }
}
