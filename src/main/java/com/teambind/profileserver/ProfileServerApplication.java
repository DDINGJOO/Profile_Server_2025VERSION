package com.teambind.profileserver;

import com.teambind.profileserver.config.TableMappingInitConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@SpringBootApplication
public class ProfileServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileServerApplication.class, args);
    }

}
