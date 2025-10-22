package com.teambind.profileserver;

import com.teambind.profileserver.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class ProfileServerApplicationTests {

    @Test
    void contextLoads() {
        // Application context가 정상적으로 로드되는지 확인
    }

}
