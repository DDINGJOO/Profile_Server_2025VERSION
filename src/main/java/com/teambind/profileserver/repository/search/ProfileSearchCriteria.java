package com.teambind.profileserver.repository.search;

import com.teambind.profileserver.enums.City;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class ProfileSearchCriteria {
    private final City city;
    private final String nickName;
    private final List<Integer> genres;       // genre ids
    private final List<Integer> instruments;  // instrument ids
    private final Character sex;
}
