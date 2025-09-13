package com.teambind.profileserver.repository;


import com.teambind.profileserver.entity.UserInstruments;
import com.teambind.profileserver.entity.key.UserInstrumentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInstrumentsRepository extends JpaRepository<UserInstruments, UserInstrumentKey>
{

}
