package com.teambind.profileserver.entity;

import com.teambind.profileserver.entity.key.UserInstrumentKey;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
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
    private UserInstrumentKey userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserInfo userInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("instrumentId")
    @JoinColumn(name = "instrumentId")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstrumentNameTable instrument;

    @Version
    @Column(name = "version")
    private int version;

}
