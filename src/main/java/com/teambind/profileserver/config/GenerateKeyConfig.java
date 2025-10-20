package com.teambind.profileserver.config;

import com.teambind.profileserver.utils.generator.PrimaryKeyGenerator;
import com.teambind.profileserver.utils.generator.impl.Snowflake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenerateKeyConfig {

    @Bean
    public PrimaryKeyGenerator PkeyGenerator() {
        return new Snowflake();
    }
}
