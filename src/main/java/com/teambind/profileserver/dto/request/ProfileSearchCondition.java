package com.teambind.profileserver.dto.request;


import com.teambind.profileserver.enums.City;
import lombok.*;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileSearchCondition {
    private City city;
    private String nickName;

    private List<Integer> genres;
    private List<Integer> instruments;
    private Character sex;
}
