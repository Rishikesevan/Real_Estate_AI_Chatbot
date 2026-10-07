package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.OwnerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerSubscriptionRepository extends JpaRepository<OwnerSubscription,Long>
{

}
