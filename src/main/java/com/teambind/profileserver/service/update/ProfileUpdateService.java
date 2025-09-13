package com.teambind.profileserver.service.update;


import com.teambind.profileserver.entity.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileUpdateService {
    public UserInfo updateProfile(UserInfo userInfo) {
        return userInfo;
    }
}
