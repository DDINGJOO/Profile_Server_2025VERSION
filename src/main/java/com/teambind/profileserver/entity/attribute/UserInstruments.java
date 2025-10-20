package com.teambind.profileserver.entity.attribute;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.base.UserAttributeBase;
import com.teambind.profileserver.entity.attribute.key.UserInstrumentKey;
import com.teambind.profileserver.entity.attribute.nameTable.InstrumentNameTable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_instruments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInstruments extends UserAttributeBase<UserInstrumentKey, InstrumentNameTable> {

    @EmbeddedId
    private UserInstrumentKey id;

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

    @Override
    public InstrumentNameTable getAttribute() {
        return instrument;
    }

    @Override
    public void setAttribute(InstrumentNameTable instrument) {
        this.instrument = instrument;
    }

}
