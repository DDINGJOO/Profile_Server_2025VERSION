package com.teambind.profileserver.events.consumer;


import com.teambind.profileserver.events.event.ProfileCreateRequest;
import com.teambind.profileserver.events.event.ProfileImageChanged;
import com.teambind.profileserver.events.event.UserDeletedEvent;
import com.teambind.profileserver.service.create.UserInfoLifeCycleService;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import com.teambind.profileserver.utils.json.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaConsumer {
    private final ProfileUpdateService service;
    private final UserInfoLifeCycleService userInfoLifeCycleService;
	private final JsonUtil jsonUtil;


    @KafkaListener(topics = "profile-image-changed" , groupId = "profile-consumer-group")
    public void profileImageChanger(String message) {
        try {
            ProfileImageChanged request = jsonUtil.fromJson(message, ProfileImageChanged.class);
            service.updateProfileImage(request.getReferenceId(), request.getImageUrl());
        } catch (Exception e) {
            // 역직렬화 실패 또는 처리 중 오류 발생 시 로깅/대응
            log.error("Failed to deserialize or process profile-create-request message: {}", message, e);
            // 필요하면 DLQ 전송이나 재시도 로직 추가
        }
    }

    @KafkaListener(topics = "user-created" , groupId = "profile-consumer-group")
    public void createUserProfile(String message) {
        try {
            ProfileCreateRequest request = jsonUtil.fromJson(message, ProfileCreateRequest.class);
            userInfoLifeCycleService.createUserProfile(request.getUserId(), request.getProvider());
        } catch (Exception e) {
            // 역직렬화 실패 또는 처리 중 오류 발생 시 로깅/대응
            log.error("Failed to deserialize or process profile-create-request message: {}", message, e);
            // 필요하면 DLQ 전송이나 재시도 로직 추가
        }
    }
	
	@KafkaListener(topics = "user-deleted" , groupId = "profile-consumer-group")
	public void deleteUserProfile(String message)
	{
		try{
			UserDeletedEvent req = jsonUtil.fromJson(message, UserDeletedEvent.class);
			userInfoLifeCycleService.deleteUserProfile(req.getUserId());
		}catch (Exception e) {
			log.error("Failed to deserialize or process user-deleted message: {}", message, e);
		}
	}
	
}
