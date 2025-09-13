package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.UserGenres;
import com.teambind.profileserver.entity.key.UserGenreKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGenresRepository extends JpaRepository <UserGenres, UserGenreKey>{
}
