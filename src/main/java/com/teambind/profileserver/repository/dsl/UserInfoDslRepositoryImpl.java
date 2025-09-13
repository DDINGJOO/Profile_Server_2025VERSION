package com.teambind.profileserver.repository.dsl;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.UserInfoDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class UserInfoDslRepositoryImpl implements UserInfoDslRepository{


    @Override
    public UserInfo findByUserIdWithInterests(String userId) {
        return null;
    }

    @Override
    public UserInfo findByUserIdWithGenres(String userId) {
        return null;
    }

    @Override
    public UserInfo findByUserIdWithInstruments(String userId) {
        return null;
    }
}
