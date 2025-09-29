package com.teambind.profileserver.repository.search;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProfileSearchCriteria {
    private final String city;
    private final String nickName;
    private final List<Integer> genres;       // 장르 ID 목록
    private final List<Integer> instruments;  // 악기 ID 목록
    private final Character sex;
}
