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
    private City city;
    private List<String> genres;
    private List<String> instruments;
}
