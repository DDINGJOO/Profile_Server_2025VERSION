package com.teambind.profileserver.config;

import com.teambind.profileserver.utils.generator.PKeyGenerator;
import com.teambind.profileserver.utils.generator.impl.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenerateKeyConfig {

    @Bean
    public PKeyGenerator PkeyGenerator() {
        return new Snowflake();
    }
}
