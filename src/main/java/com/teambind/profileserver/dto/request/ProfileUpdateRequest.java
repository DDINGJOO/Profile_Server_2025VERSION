package com.teambind.profileserver.dto.request;

import com.teambind.profileserver.enums.City;
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




    private City city;

    private boolean chattable;
    private boolean publicProfile;

    private Map<Integer,String> genres;
    private Map<Integer,String> instruments;
}
