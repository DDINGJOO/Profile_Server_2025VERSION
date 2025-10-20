package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.attribute.UserGenres;
import com.teambind.profileserver.entity.attribute.key.UserGenreKey;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGenresRepository extends JpaRepository<UserGenres, UserGenreKey> {

    @Query("select ug.id.genreId from UserGenres ug where ug.id.userId = :userId")
    List<Integer> findGenreIdsById(@Param("userId") String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserGenres ug where ug.id.userId = :userId and ug.id.genreId in :genreIds")
    int deleteByIdAndGenreIdsIn(@Param("userId") String userId, @Param("genreIds") Collection<Integer> genreIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from UserGenres ug where ug.id.userId = :userId")
    int deleteById(@Param("userId") String userId);


}
