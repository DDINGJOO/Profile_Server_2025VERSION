package com.teambind.profileserver.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class NicknameValidator {

    private final NickNameValidatorService nickNameValidatorService;


    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateNickname(@RequestParam("type") String type , @RequestParam("value") String value) {
        if(type.equals("nickname")){return ResponseEntity.ok(nickNameValidatorService.validateNickname(value));}
        return ResponseEntity.ok(false);
    }
}

