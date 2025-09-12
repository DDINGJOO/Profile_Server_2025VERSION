package com.teambind.profileserver.entity.key;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class UserGenreKey implements Serializable {

    private String userId;
    private int genreId;
}
