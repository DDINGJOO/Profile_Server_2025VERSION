package com.teambind.profileserver.service.search;

import com.teambind.profileserver.dto.response.BatchUserSummaryResponse;
import com.teambind.profileserver.dto.response.UserResponse;
import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.exceptions.ErrorCode;
import com.teambind.profileserver.exceptions.ProfileException;
import com.teambind.profileserver.repository.ProfileSearchRepository;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import java.util.List;
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
        if(userInfo == null)
            throw new ProfileException(ErrorCode.USER_NOT_FOUND);
        return UserResponse.fromEntity(userInfo);
    }

	
	// 페이징 처리 서능 테스트용 메서드
    @Transactional(readOnly = true)
    public Page<UserResponse> searchProfiles(ProfileSearchCriteria criteria, Pageable pageable) {
        var result = repository.search(criteria, pageable);
        return result.map(UserResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Slice<UserResponse> searchProfilesByCursor(ProfileSearchCriteria criteria, String cursor, int size) {
        var result = repository.searchByCursor(criteria, cursor, size);
        return result.map(UserResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<BatchUserSummaryResponse> searchProfilesByIds(List<String> userIds) {
        var users = repository.searchByUserIds(userIds);
        return users.stream().map(BatchUserSummaryResponse::fromEntity).toList();
    }
}
