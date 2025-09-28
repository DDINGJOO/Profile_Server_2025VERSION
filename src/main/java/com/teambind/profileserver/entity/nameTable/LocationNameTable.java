package com.teambind.profileserver.entity.nameTable;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "location_names")
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LocationNameTable {
    @Id
    @Column(name = "city_id")
    private String id;

    @Column(name ="city_name")
    private String city;
}
