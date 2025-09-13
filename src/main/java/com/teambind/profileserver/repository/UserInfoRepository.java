package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {
    long countByUserIdStartingWith(String prefix);
}
