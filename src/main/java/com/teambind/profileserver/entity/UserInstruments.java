package com.teambind.profileserver.entity;


import com.teambind.profileserver.entity.key.UserInstrumentKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "instruments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInstruments {

    @EmbeddedId
    private UserInstrumentKey id;
    @Version
    @Column(name="version")
    private int version;

}
