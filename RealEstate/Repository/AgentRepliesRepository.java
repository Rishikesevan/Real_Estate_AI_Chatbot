package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.AgentReplies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentRepliesRepository extends JpaRepository<AgentReplies, Long> {

    List<AgentReplies> findByEnquiry_User_Id(Long userId);
}
