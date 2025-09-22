package com.teambind.profileserver.factory;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 테스트 실행 전에(테스트 ApplicationContext 기동 시) 한 번만 대량 데이터를 준비하는 초기화 컴포넌트.
 * - 테스트 소스(src/test/java) 하위에만 존재하므로 운영 빌드/런타임에는 포함되지 않습니다.
 * - UserInfoFactory.ensureUsersGenerated를 통해 이미 생성된 데이터를 재사용하고, 부족분만 채웁니다.
 *
 * 구성(환경변수/시스템프로퍼티)으로 제어 가능:
 * - profile.test.seed-enabled=true 여야 동작 (기본 비활성화)
 * - profile.test.seed-users: 생성 목표 유저 수 (기본 200)
 * - profile.test.seed-batch: 배치 사이즈 (기본 50)
 * 예) ./gradlew test -Dprofile.test.seed-enabled=true -Dprofile.test.seed-users=20000 -Dprofile.test.seed-batch=2000
 */
@Component
@ConditionalOnProperty(name = "profile.test.seed-enabled", havingValue = "true")
public class TestDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(TestDataInitializer.class);

    private final UserInfoFactory userInfoFactory;
    private final Environment env;

    public TestDataInitializer(UserInfoFactory userInfoFactory, Environment env) {
        this.userInfoFactory = userInfoFactory;
        this.env = env;
    }

    @PostConstruct
    public void init() {
        int total = getIntProp("profile.test.seed-users", 200);
        int batch = getIntProp("profile.test.seed-batch", 10);
        log.info("[TestDataInitializer] Ensuring test data exists: total={}, batch={}", total, batch);
        userInfoFactory.ensureUsersGenerated(total, batch);
    }

    private int getIntProp(String key, int def) {
        try {
            String v = env.getProperty(key);
            if (v == null || v.isBlank()) return def;
            return Integer.parseInt(v.trim());

        } catch (Exception e) {
            log.warn("Invalid int property '{}', using default {}. cause={}", key, def, e.toString());
            return def;
        }
    }
}
