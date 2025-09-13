package com.teambind.profileserver.entity;

import com.teambind.profileserver.entity.key.UserInstrumentKey;
import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_instruments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInstruments {

    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "userId", column = @Column(name = "user_id")),
            @AttributeOverride(name = "instrumentId", column = @Column(name = "instrument_id"))
    })
    private UserInstrumentKey userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserInfo userInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("instrumentId")
    @JoinColumn(name = "instrument_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private InstrumentNameTable instrument;

    @Version
    @Column(name = "version")
    private int version;

}
