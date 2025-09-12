package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.UserGenres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGenresRepository extends JpaRepository <UserGenres, String>{
}
