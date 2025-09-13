package com.teambind.profileserver.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teambind.profileserver.repository.UserInfoDslRepository;
import com.teambind.profileserver.repository.dsl.UserInfoDslRepositoryImpl;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuerydslConfig {
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    @Bean
    public UserInfoDslRepository userInfoDslRepository(UserInfoDslRepositoryImpl impl) {
        return impl;
    }
}
