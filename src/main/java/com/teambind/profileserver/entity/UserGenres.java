package com.teambind.profileserver.entity;


import com.teambind.profileserver.entity.key.UserGenreKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_genres")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserGenres
{

    @EmbeddedId
    private UserGenreKey id;

    @Column(name = "genre_id")
    private int genreId;
}
