package com.teambind.profileserver.entity.nameTable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instrument_name")
public class InstrumentNameTable {
    @Id
    @Column(name = "instrument_id")
    private int instrumentId;

    @Column(name = "instrument_name")
    private String instrumentName;

}
