package com.teambind.profileserver.entity.key;


import com.teambind.profileserver.entity.UserInfo;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class UserGenreKey implements Serializable {


    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;
    private int genreId;
}
