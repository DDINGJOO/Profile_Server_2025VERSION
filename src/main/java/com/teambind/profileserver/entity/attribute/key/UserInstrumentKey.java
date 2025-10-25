package com.teambind.profileserver.entity.attribute.key;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserInstrumentKey implements Serializable {

  private String userId;
  private int instrumentId;

  public UserInstrumentKey(String userId, Integer name) {
    this.userId = userId;
    this.instrumentId = name;
  }
}
