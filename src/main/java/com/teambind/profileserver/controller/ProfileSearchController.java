package com.teambind.profileserver.controller;


import com.teambind.profileserver.dto.response.BatchUserSummaryResponse;
import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import com.teambind.profileserver.service.search.ProfileSearchService;
import java.util.List;
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
	public ResponseEntity<Slice<UserResponse>> searchProfiles(
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String nickName,
			@RequestParam(required = false) List<Integer> genres,
			@RequestParam(required = false) List<Integer> instruments,
			@RequestParam(required = false) Character sex,
			@RequestParam(required = false) String cursor,
			@RequestParam(required = false, defaultValue = "10") int size) {
		
		if (cursor != null && cursor.isBlank()) cursor = null;
		if (size <= 0) size = 10;
		if (size > 100) size = 100;
		
		ProfileSearchCriteria criteria = ProfileSearchCriteria.builder()
				.city(city)
				.nickName(nickName)
				.genres(genres)
				.instruments(instruments)
				.sex(sex)
				.build();
		
		Slice<UserResponse> result = profileSearchService.searchProfilesByCursor(criteria, cursor, size);
		return ResponseEntity.ok(result);
	}

    @PostMapping("/batch")
    public ResponseEntity<List<BatchUserSummaryResponse>> getProfilesBatch(@RequestBody List<String> userIds) {
        var result = profileSearchService.searchProfilesByIds(userIds);
        return ResponseEntity.ok(result);
    }
}
