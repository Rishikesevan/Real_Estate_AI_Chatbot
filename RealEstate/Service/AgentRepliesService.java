package com.project.RealEstate.Service;

import com.project.RealEstate.Entity.AgentReplies;
// import com.project.RealEstate.Entity.Enquiry;
// import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.AgentRepliesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentRepliesService {

    @Autowired
    private AgentRepliesRepository agentRepliesRepository;

    public List<AgentReplies> getAgentRepliesByUserId(Long userId) {
        return agentRepliesRepository.findByEnquiry_User_Id(userId);
    }
}
