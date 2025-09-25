package com.teambind.profileserver.dto.response;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.enums.City;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String userId;
    private Character sex;
    private String profileImageUrl;
    private List<String> genres;
    private List<String> instruments;
    private City city;
    private String nickname;
    private Boolean isChattable;
    private Boolean isPublic;

    public static UserResponse fromEntity(UserInfo userInfo){
        return UserResponse.builder()
                .userId(userInfo.getUserId())
                .city(userInfo.getCity())
                .nickname(userInfo.getNickname())
                .isChattable(userInfo.getIsChatable())
                .isPublic(userInfo.getIsPublic())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .sex(userInfo.getSex())
                .instruments(userInfo.getUserInstruments().stream().map(ui -> ui.getInstrument().getInstrumentName()).toList())
                .genres(userInfo.getUserGenres().stream().map(ug -> ug.getGenre().getGenreName()).toList())
                .build();
    }
}
