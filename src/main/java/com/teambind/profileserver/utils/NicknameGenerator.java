package com.teambind.profileserver.utils;


import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NicknameGenerator {
    public static String generateNickname(String provider) {
        String nickname = UUID.randomUUID().toString().substring(0, 13);
        return provider.toUpperCase() + "_"+ nickname;
    }
}


