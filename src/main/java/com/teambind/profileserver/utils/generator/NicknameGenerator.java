package com.teambind.profileserver.utils.generator;


import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class NicknameGenerator {
    public static String generateNickname(String provider) {
        String nickname = UUID.randomUUID().toString().substring(0, 13);
        return provider.toUpperCase() + "_"+ nickname;
    }
}


