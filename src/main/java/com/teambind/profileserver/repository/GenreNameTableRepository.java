package com.teambind.profileserver.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreNameTableRepository extends JpaRepository<GenreNameTableRepository, Integer> {
}
