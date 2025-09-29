package com.teambind.profileserver.controller;


import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.service.search.ProfileSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles/profiles")
public class ProfileSearchController {

    private final ProfileSearchService profileSearchService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getProfiles(@PathVariable("userId") String userId) {
        var response = profileSearchService.searchProfileById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("")
    public ResponseEntity<Slice<UserResponse>> searchProfiles(@RequestParam ProfileSearchCriteria criteria,
                                                              @RequestParam(required = false) String cursor,
                                                              @RequestParam int size) {
        if (cursor != null && cursor.isBlank()) cursor = null;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
        Slice<UserResponse> result = profileSearchService.searchProfilesByCursor(criteria, cursor, size);
        return ResponseEntity.ok(result);
    }
}
