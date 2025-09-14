package com.teambind.profileserver.controller;


import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileUpdateController {
    private final ProfileUpdateService profileUpdateService;


    @PutMapping("/ver1")
    public ResponseEntity<Boolean> updateProfile(@RequestParam String userId, @RequestBody ProfileUpdateRequest request) {
        profileUpdateService.updateProfile(userId, request.getNickname(),request.getInstruments(),request.getGenres());
        return ResponseEntity.ok(true);
    }

    @PutMapping("/ver2")
    public ResponseEntity<Boolean> updateProfileAll(@RequestParam String userId, @RequestBody ProfileUpdateRequest request) {
        profileUpdateService.updateProfileAll(userId, request.getNickname(),request.getInstruments(),request.getGenres());
        return ResponseEntity.ok(true);
    }
}
