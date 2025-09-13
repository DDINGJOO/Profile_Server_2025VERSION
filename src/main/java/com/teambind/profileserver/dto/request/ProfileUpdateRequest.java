package com.teambind.profileserver.dto.request;

import com.teambind.profileserver.enums.City;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private String nickname;
    private City city;
    private List<Integer> genres;
    private List<Integer> instruments;
}
