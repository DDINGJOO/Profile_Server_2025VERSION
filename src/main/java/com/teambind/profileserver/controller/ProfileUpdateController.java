package com.teambind.profileserver.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileUpdateController {


    @PutMapping("")
    public String updateProfile(@RequestParam String userId) {
        System.out.println("Profile Updated");
        return "Profile Updated";
    }
}
