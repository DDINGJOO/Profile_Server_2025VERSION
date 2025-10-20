package com.teambind.profileserver.entity.attribute;

import com.teambind.profileserver.entity.UserInfo;
import com.teambind.profileserver.entity.attribute.key.UserGenreKey;
import com.teambind.profileserver.entity.attribute.nameTable.GenreNameTable;
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
    private UserGenreKey id;

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
