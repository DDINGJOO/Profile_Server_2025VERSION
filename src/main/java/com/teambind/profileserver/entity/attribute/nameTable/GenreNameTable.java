package com.teambind.profileserver.entity.attribute.nameTable;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "genre_name")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenreNameTable {
    @Id
    @Column(name = "genre_id")
    private int genreId;

    @Column(name = "genre_name")
    private String genreName;

}
