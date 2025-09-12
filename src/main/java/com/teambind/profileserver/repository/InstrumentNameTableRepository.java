package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.nameTable.InstrumentNameTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentNameTableRepository extends JpaRepository<InstrumentNameTable, Integer> {
}
