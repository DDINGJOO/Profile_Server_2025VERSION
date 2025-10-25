package com.teambind.profileserver.dto.response;

import com.teambind.profileserver.entity.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchUserSummaryResponse {
  private String userId;
  private String nickname;
  private String profileImageUrl;

  public static BatchUserSummaryResponse fromEntity(UserInfo userInfo) {
    return BatchUserSummaryResponse.builder()
        .userId(userInfo.getUserId())
        .nickname(userInfo.getNickname())
        .profileImageUrl(userInfo.getProfileImageUrl())
        .build();
  }
}
