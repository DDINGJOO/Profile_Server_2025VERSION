package com.teambind.profileserver.dto.request;

import com.teambind.profileserver.validator.Attribute;
import com.teambind.profileserver.validator.NickName;
import java.util.List;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
	@NickName
    private String nickname;
    private String city;
	private String introduction;
    private boolean chattable;
    private boolean publicProfile;
    private Character sex;

	@Attribute(value = "GENRE")
    private List<Integer> genres;
	@Attribute(value = "INTEREST")
    private List<Integer> instruments;
	
}
