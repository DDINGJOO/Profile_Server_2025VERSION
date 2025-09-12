package com.teambind.profileserver.entity.key;


import com.teambind.profileserver.entity.UserInfo;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserInstrumentKey implements Serializable  {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;


    private int instrumentId;

}
