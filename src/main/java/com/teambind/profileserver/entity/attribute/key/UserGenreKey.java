package com.teambind.profileserver.entity.attribute.key;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class UserGenreKey implements Serializable {

  private String userId;
  private int genreId;

  public UserGenreKey(String userId, int genreId) {
    this.userId = userId;
    this.genreId = genreId;
  }
}
