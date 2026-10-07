package com.project.RealEstate.Repository;

import com.project.RealEstate.Entity.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, Long>
{

    List<Enquiry> findByAgentId(Long agentId);
}
