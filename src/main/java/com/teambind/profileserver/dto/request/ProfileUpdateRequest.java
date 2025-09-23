package com.teambind.profileserver.dto.request;

import com.teambind.profileserver.enums.City;
import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private String nickname;
    private City city;


    private Map<Integer,String> genres;
    private Map<Integer,String> instruments;
}
