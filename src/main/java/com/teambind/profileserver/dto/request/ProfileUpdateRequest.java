package com.teambind.profileserver.dto.request;

import java.util.Map;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private String nickname;
    private String city;

    private boolean chattable;
    private boolean publicProfile;
    private Character sex;

    private Map<Integer,String> genres;
    private Map<Integer,String> instruments;
}
