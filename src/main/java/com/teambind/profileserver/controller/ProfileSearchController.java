package com.teambind.profileserver.controller;


import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.service.search.ProfileSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class ProfileSearchController {

    private final ProfileSearchService profileSearchService;
    @GetMapping("")
    public String getProfiles(@RequestParam ProfileSearchCriteria criteria) {
        return "Profiles : " + profileSearchService.searchProfiles(criteria, null);
    }

    @GetMapping("/search-by-filter")
    public String searchProfiles( @RequestParam ProfileSearchCriteria criteria, String cursor, int size) {
        if (cursor == null || cursor.isBlank()) cursor = null;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
        if (size < 1) size = 1;
        return "Profiles : " + profileSearchService.searchProfilesByCursor(criteria, cursor, size);
    }


}
