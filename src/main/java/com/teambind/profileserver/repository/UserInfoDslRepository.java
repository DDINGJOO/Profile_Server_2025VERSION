package com.teambind.profileserver.repository;

import com.teambind.profileserver.entity.UserInfo;

public interface UserInfoDslRepository {
    UserInfo findByUserIdWithInterests(String userId);
    UserInfo findByUserIdWithGenres(String userId);
    UserInfo findByUserIdWithInstruments(String userId);
}
