package com.teambind.profileserver.controller;

import com.teambind.profileserver.dto.request.ProfileUpdateRequest;
import com.teambind.profileserver.service.update.ProfileUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileUpdateController {
  private final ProfileUpdateService profileUpdateService;

  @PutMapping("/{userId}")
  public ResponseEntity<Boolean> updateProfile(
      @PathVariable String userId, @Valid @RequestBody ProfileUpdateRequest request) {
    profileUpdateService.updateProfile(userId, request);
    return ResponseEntity.ok(true);
  }
}
