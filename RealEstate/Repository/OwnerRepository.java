package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    List<Owner> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Owner findByToken(String token);

    @Query("SELECT u FROM Owner u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<Owner> getOwnerByEmail(@Param("email") String email);
}
