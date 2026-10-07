package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.AgentSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentSubscriptionRepository extends JpaRepository<AgentSubscription, Long> {
    boolean existsByAgentIdAndActiveTrue(Long agentId);

    Optional<AgentSubscription> findTopByAgentIdOrderByEndDateDesc(Long agentId);
}
