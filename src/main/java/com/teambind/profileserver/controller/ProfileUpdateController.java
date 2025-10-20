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
    private final ProfileUpdateValidator profileUpdateValidator;


    /**
     * 프로필을 부분 업데이트합니다. (PATCH 방식)
     * request에서 null이 아닌 필드만 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param request 업데이트할 프로필 정보
     * @return 성공 여부
     */
    @PutMapping("/{userId}/ver1")
    public ResponseEntity<Boolean> updateProfile(@PathVariable String userId, @RequestBody ProfileUpdateRequest request)  {
        profileUpdateValidator.validateProfileUpdateRequest(
                request.getNickname(),
                request.getGenres(),
                request.getInstruments()
        );
        profileUpdateService.updateProfile(userId, request);
        return ResponseEntity.ok(true);
    }

    /**
     * 프로필을 전체 업데이트합니다. (PUT 방식)
     * 모든 필드를 request의 값으로 교체합니다.
     *
     * @param userId 사용자 ID
     * @param request 업데이트할 프로필 정보
     * @return 성공 여부
     */
    @PutMapping("/{userId}/ver2")
    public ResponseEntity<Boolean> updateProfileAll(@PathVariable String userId, @RequestBody ProfileUpdateRequest request)  {
        profileUpdateValidator.validateProfileUpdateRequest(
                request.getNickname(),
                request.getGenres(),
                request.getInstruments()
        );
        profileUpdateService.updateProfileAll(userId, request);
        return ResponseEntity.ok(true);
    }
}


