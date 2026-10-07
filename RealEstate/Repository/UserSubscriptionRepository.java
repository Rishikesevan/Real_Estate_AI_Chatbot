package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.UserSubscription;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE UserSubscription s SET s.active = false WHERE s.user.id = :userId")
    void deactivateAllByUserId(@Param("userId") Long userId);

    Optional<UserSubscription> findTopByUserIdOrderByEndDateDesc(Long userId);
}
