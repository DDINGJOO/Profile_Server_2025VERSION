package com.teambind.profileserver.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileSearchController {

    @GetMapping("")
    public String getProfiles( @RequestParam String userId) {
        return "Profile : " + userId;
    }

    @GetMapping("/search")
    public String searchProfiles( ) {
        return "Profile : ";
    }


}
