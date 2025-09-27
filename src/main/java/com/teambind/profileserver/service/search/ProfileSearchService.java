package com.teambind.profileserver.service.search;

import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.ProfileSearchRepository;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileSearchService {

    private final ProfileSearchRepository repository;

    @Transactional(readOnly = true)
    public UserResponse searchProfileById(String userId) {
        UserInfo userInfo = repository.search(userId);
        return UserResponse.fromEntity(userInfo);


    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchProfiles(ProfileSearchCriteria criteria, Pageable pageable) {
        var result =  repository.search(criteria, pageable);
        return result.map(UserResponse::fromEntity);
    }


    //TODO : implement cursor-based pagination
    @Transactional(readOnly = true)
    public List<UserResponse> searchProfilesByCursor(ProfileSearchCriteria criteria, String cursor, int size) {
        var result =  repository.searchByCursor(criteria, cursor, size);
        return result.getContent().stream().map(UserResponse::fromEntity).collect(Collectors.toList());
    }
}
