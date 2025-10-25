package com.teambind.profileserver.events.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileCreateRequest extends Event {
  private String userId;
  private String provider;

  public ProfileCreateRequest(String userId, String provider) {
    super("profile-created");
  }
}
