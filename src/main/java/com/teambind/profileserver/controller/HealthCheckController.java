package com.teambind.profileserver.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/health")
public class HealthCheckController {
    @GetMapping()
    public String healthCheck() {

        log.info("Server is running");
        return "Server is Running";
    }
}
