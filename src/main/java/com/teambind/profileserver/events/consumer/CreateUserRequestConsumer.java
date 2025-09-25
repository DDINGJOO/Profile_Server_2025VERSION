package com.teambind.profileserver.events.consumer;


import com.teambind.profileserver.events.event.ProfileCreateRequest;
import com.teambind.profileserver.service.create.CreateUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;

@RequiredArgsConstructor
public class CreateUserRequestConsumer {
    private final CreateUserProfile createUserProfile;


    @KafkaListener(topics = "profile-create-request")
    public void createUserProfile(ProfileCreateRequest request) {
        createUserProfile.createUserProfile(request.getUserId(), request.getProvider());
        return;
    }

}
