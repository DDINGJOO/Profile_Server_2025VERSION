package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.UserGenres;
import com.teambind.profileserver.entity.key.UserGenreKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserGenresRepository extends JpaRepository<UserGenres, UserGenreKey> {

    @Query("select ug.userId.genreId from UserGenres ug where ug.userId.userId = :userId")
    List<Integer> findGenreIdsByUserId(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserGenres ug where ug.userId.userId = :userId and ug.userId.genreId in :genreIds")
    int deleteByUserIdAndGenreIdsIn(@Param("userId") String userId, @Param("genreIds") Collection<Integer> genreIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserGenres ug where ug.userId.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
}
