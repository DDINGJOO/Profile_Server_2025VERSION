package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.UserInstruments;
import com.teambind.profileserver.entity.key.UserInstrumentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserInstrumentsRepository extends JpaRepository<UserInstruments, UserInstrumentKey> {

    @Query("select ui.userId.instrumentId from UserInstruments ui where ui.userId.userId = :userId")
    List<Integer> findInstrumentIdsByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserInstruments ui where ui.userId.userId = :userId and ui.userId.instrumentId in :instrumentIds")
    int deleteByUserIdAndInstrumentIdsIn(@Param("userId") String userId, @Param("instrumentIds") Collection<Integer> instrumentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserInstruments ui where ui.userId.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
}
