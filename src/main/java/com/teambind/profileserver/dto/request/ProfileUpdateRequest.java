package com.teambind.profileserver.dto.request;

import java.util.Collections;
import java.util.List;
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
	private String introduction;
    private boolean chattable;
    private boolean publicProfile;
    private Character sex;

    private List<Integer> genres;
    private List<Integer> instruments;
	
}
