package com.teambind.profileserver.controller;


import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles/profiles")
@RequiredArgsConstructor
public class ProfileUpdateController {
    private final ProfileUpdateService profileUpdateService;



    @PutMapping("/{userId}/ver1")
    public ResponseEntity<Boolean> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest request)  {
        profileUpdateService.updateProfile(userId, request);
        return ResponseEntity.ok(true);
    }

	// need Old Api Address
    @PutMapping("/{userId}/ver2")
    public ResponseEntity<Boolean> updateProfileAll(@PathVariable String userId, @RequestBody ProfileUpdateRequest request)  {
        profileUpdateService.updateProfile(userId, request);
        return ResponseEntity.ok(true);
    }
}


