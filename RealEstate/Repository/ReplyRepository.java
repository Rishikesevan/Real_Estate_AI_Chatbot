package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.AgentReplies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<AgentReplies, Long>
{

}
