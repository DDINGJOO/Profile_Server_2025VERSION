package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.nameTable.GenreNameTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreNameTableRepository extends JpaRepository<GenreNameTable, Integer> {
}
