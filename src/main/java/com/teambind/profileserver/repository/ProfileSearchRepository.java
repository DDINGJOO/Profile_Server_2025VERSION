package com.teambind.profileserver.repository;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.repository.search.ProfileSearchCriteria;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProfileSearchRepository {
    UserInfo search (String userId);
    Page<UserInfo> search(ProfileSearchCriteria criteria, Pageable pageable);
    Slice<UserInfo> searchByCursor(ProfileSearchCriteria criteria, String cursor, int size);
    List<UserInfo> searchByUserIds(List<String> userIds);
}
