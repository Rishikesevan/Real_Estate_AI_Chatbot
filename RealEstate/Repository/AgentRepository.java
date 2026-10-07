package com.project.RealEstate.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.RealEstate.Entity.Agent;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    @Query("""
                SELECT a FROM Agent a
                WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(a.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<Agent> searchAgents(@Param("keyword") String keyword);

    List<Agent> findByEmail(String email);

    Agent getAgentById(Long agentId);

    Agent findByToken(String token);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("SELECT u FROM Agent u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<Agent> getUserByEmail(@Param("email") String email);
}
