package com.teambind.profileserver.controller;


import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.service.search.ProfileSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileSearchController {

    private final ProfileSearchService profileSearchService;
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getProfiles(@PathVariable String id) {
        var Response = profileSearchService.searchProfileById(id);
        return ResponseEntity.ok(Response);
    }

    @GetMapping("")
    public ResponseEntity<List<UserResponse>> searchProfiles(@RequestParam ProfileSearchCriteria criteria, @RequestParam String cursor, @RequestParam int size) {
        if (cursor == null || cursor.isBlank()) cursor = null;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
        if (size < 1) size = 1;
        var result= profileSearchService.searchProfilesByCursor(criteria, cursor, size);
        return ResponseEntity.ok(result);
    }

}
