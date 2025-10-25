package com.teambind.profileserver.events.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDeletedEvent extends Event {
  String userId;

  public UserDeletedEvent(String userId) {
    super("user-deleted");
    this.userId = userId;
  }
}
