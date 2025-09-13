package com.teambind.profileserver.entity;

import com.teambind.profileserver.entity.key.UserGenreKey;
import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_genres")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserGenres {

    @EmbeddedId
    private UserGenreKey userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UserInfo userInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("genreId")
    @JoinColumn(name = "genre_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private GenreNameTable genre;

    @Version
    @Column(name = "version")
    private int version;
}
