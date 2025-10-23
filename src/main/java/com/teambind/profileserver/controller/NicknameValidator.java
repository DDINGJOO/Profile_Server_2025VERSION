package com.teambind.profileserver.controller;


import com.teambind.profileserver.service.update.ProfileUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/profiles")
public class NicknameValidator {
	private final ProfileUpdateService profileUpdateService;


    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateNickname(@RequestParam("type") String type , @RequestParam("value") String value) {
        if(type.equals("nickname")){return ResponseEntity.ok(profileUpdateService.isNickNameExist(value));}
        return ResponseEntity.ok(false);
    }
}

