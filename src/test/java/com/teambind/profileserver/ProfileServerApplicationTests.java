package com.teambind.profileserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;

@SpringBootTest
@Profile("dev")
class ProfileServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
