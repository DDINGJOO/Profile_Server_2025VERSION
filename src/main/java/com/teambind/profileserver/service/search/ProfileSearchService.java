package com.teambind.profileserver.service.search;

import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.ProfileSearchRepository;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<UserInfo> searchProfiles(ProfileSearchCriteria criteria, Pageable pageable) {
        return repository.search(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public Slice<UserInfo> searchProfilesByCursor(ProfileSearchCriteria criteria, String cursor, int size) {
        return repository.searchByCursor(criteria, cursor, size);
    }
}
