package com.teambind.profileserver.repository.search;

import com.teambind.profileserver.enums.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
@Builder
public class ProfileSearchCriteria {
    private final City city;
    private final String nickName;
    private final List<Integer> genres;       // 장르 ID 목록
    private final List<Integer> instruments;  // 악기 ID 목록
    private final Character sex;
}
