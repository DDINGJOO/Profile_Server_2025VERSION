package com.teambind.profileserver.utils;


import org.springframework.stereotype.Component;

@Component
public interface PKeyGenerator {
    String generateKey();
}
