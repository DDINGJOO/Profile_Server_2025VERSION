package com.teambind.profileserver.events.consumer;


import com.teambind.profileserver.service.create.CreateUserProfile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InitUserProfile {
    private final CreateUserProfile createUserProfile;


    private String userId;
    private String provider;


    public void createUserProfile(String userId, String provider) {
        createUserProfile.createUserProfile(userId, provider);
        return;
    }

}
