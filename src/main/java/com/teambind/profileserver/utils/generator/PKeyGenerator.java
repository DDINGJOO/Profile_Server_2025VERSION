package com.teambind.profileserver.utils.generator;


import org.springframework.stereotype.Component;

@Component
public interface PKeyGenerator {
    String generateKey();
}
