package com.teambind.profileserver.utils;

import com.teambind.profileserver.utils.generator.NicknameGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



class NicknameGeneratorTest {

    private NicknameGenerator nicknameGenerator;

    @Test
    @DisplayName("닉네임 생성 테스트")
    void generateNickname() {
        String nickname = NicknameGenerator.generateNickname("facebook");
        String nicknameAfter_ = nickname.substring(nickname.indexOf("_")+1);
        assertTrue(nickname.startsWith("FACEBOOK_"));
        assertEquals(13, nicknameAfter_.length());
    }

    @Test
    @DisplayName("서로 다른 닉네임 생성 테스트 ")
    void generateNickname_different() {
        String nickname1 = NicknameGenerator.generateNickname("facebook");
        String nickname2 = NicknameGenerator.generateNickname("facebook");
        assertNotEquals(nickname1, nickname2);
    }
}
