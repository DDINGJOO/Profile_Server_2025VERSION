package com.teambind.profileserver.events.consumer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.teambind.profileserver.events.event.ProfileCreateRequest;
import com.teambind.profileserver.events.event.ProfileImageChanged;
import com.teambind.profileserver.service.create.CreateUserProfile;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaConsumer {
    private final ProfileUpdateService service;
    private final CreateUserProfile createUserProfile;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "profile-image-changed" , groupId = "profile-consumer-group")
    public void profileImageChanger(String message) {
        try {
            ProfileImageChanged request = objectMapper.readValue(message, ProfileImageChanged.class);
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
            ProfileCreateRequest request = objectMapper.readValue(message, ProfileCreateRequest.class);
            createUserProfile.createUserProfile(request.getUserId(), request.getProvider());
        } catch (Exception e) {
            // 역직렬화 실패 또는 처리 중 오류 발생 시 로깅/대응
            log.error("Failed to deserialize or process profile-create-request message: {}", message, e);
            // 필요하면 DLQ 전송이나 재시도 로직 추가
        }
    }
}
