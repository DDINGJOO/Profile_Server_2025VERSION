package com.teambind.profileserver.service.create;


import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.utils.generator.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateUserProfile {
    private final UserInfoRepository userInfoRepository;



    public String createUserProfile(String userId, String provider) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .nickname(NicknameGenerator.generateNickname(provider))
                .city(null)
                .isChatable(false)
                .isPublic(false)
                .sex(null)
                .profileImageUrl(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userInfoRepository.save(userInfo);
        return userInfo.getUserId();
    };
}
