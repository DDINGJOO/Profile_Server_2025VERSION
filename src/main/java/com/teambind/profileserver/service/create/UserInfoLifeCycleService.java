package com.teambind.profileserver.service.create;


import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ProfileErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.UserInfoRepository;
import com.teambind.profileserver.utils.generator.NicknameGenerator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoLifeCycleService {
    private final UserInfoRepository userInfoRepository;



    @Transactional
    public void createUserProfile(String userId, String provider) {
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .nickname(NicknameGenerator.generateNickname(provider))
                .city(null)
                .isChatable(false)
                .isPublic(false)
		        .introduction(null)
                .sex(null)
                .profileImageUrl(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userInfoRepository.save(userInfo);
    };
	
	@Transactional
	public void deleteUserProfile(String userId) {
		UserInfo userInfo = userInfoRepository.findById(userId).orElseThrow(
				() -> new ProfileException(ProfileErrorCode.USER_NOT_FOUND)
		);
		userInfoRepository.delete(userInfo);
	}
}
