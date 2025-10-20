package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.attribute.UserInstruments;
import com.teambind.profileserver.entity.attribute.key.UserInstrumentKey;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInstrumentsRepository extends JpaRepository<UserInstruments, UserInstrumentKey> {

    @Query("select ui.id.instrumentId from UserInstruments ui where ui.id.userId = :userId")
    List<Integer> findInstrumentIdsByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserInstruments ui where ui.id.userId = :userId and ui.id.instrumentId in :instrumentIds")
    int deleteByUserIdAndInstrumentIdsIn(@Param("userId") String userId, @Param("instrumentIds") Collection<Integer> instrumentIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserInstruments ui where ui.id.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
}
