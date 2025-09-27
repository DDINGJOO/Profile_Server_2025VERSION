package com.teambind.profileserver.controller;


import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import com.teambind.profileserver.utils.validator.ProfileUpdateValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileUpdateController {
    private final ProfileUpdateService profileUpdateService;
    private final ProfileUpdateValidator profileUpdateValidator;


    @PutMapping("/{userId}/ver1")
    public ResponseEntity<Boolean> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest request)  {

        profileUpdateValidator.validateProfileUpdateRequest(request.getNickname(), request.getInstruments(), request.getGenres());
        profileUpdateService.updateProfile(userId, request.getNickname(), request.getInstruments().keySet().stream().toList(),request.getGenres().keySet().stream().toList(),request.isChattable(),request.isPublicProfile());
        return ResponseEntity.ok(true);
    }

    @PutMapping("/{userId}/ver2")
    public ResponseEntity<Boolean> updateProfileAll(@PathVariable String userId, @RequestBody ProfileUpdateRequest request)  {
        profileUpdateValidator.validateProfileUpdateRequest(request.getNickname(), request.getInstruments(), request.getGenres());
        profileUpdateService.updateProfileAll(userId, request.getNickname(),request.getInstruments().keySet().stream().toList(),request.getGenres().keySet().stream().toList(), request.isChattable(), request.isPublicProfile());
        return ResponseEntity.ok(true);
    }
}


