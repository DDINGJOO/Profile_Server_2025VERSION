package com.teambind.profileserver.repository;

import com.teambind.profileserver.entity.nameTable.LocationNameTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationNameTableRepository extends JpaRepository<LocationNameTable,String> {
}
