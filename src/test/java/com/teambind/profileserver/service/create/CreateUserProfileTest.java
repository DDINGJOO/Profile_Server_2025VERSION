package com.teambind.profileserver.service.create;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.UserInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = com.teambind.profileserver.ProfileServerApplication.class)
class CreateUserProfileTest {
    @Autowired
    private CreateUserProfile createUserProfile;
    @Autowired
    private UserInfoRepository userInfoRepository;



    @AfterEach
    void tearDown() {
        userInfoRepository.deleteAll();
    }
    @Test
    @DisplayName("유저 초기 생성 테스트 ")
    void createUserProfile() {
        String userId = "testUserId";
        String provider = "facebook";
        String nickname = createUserProfile.createUserProfile(userId, provider);
        assertNotNull(nickname);

        UserInfo userInfo = userInfoRepository.findById(userId).orElse(null);
        assertNotNull(userInfo);
        assertEquals(userId, userInfo.getUserId());
        assertTrue(userInfo.getNickname().startsWith("FACEBOOK_"));
    }
}
