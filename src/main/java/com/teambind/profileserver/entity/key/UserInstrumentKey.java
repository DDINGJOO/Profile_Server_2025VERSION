package com.teambind.profileserver.entity.key;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserInstrumentKey implements Serializable  {

    private String userId;
    private int instrumentId;

}
