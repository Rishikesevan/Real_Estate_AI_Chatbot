package com.project.RealEstate.Service;

import com.project.RealEstate.Entity.AgentReplies;
import com.project.RealEstate.Entity.Enquiry;
import com.project.RealEstate.Repository.EnquiryRepository;
import com.project.RealEstate.Repository.ReplyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReplyService
{
    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private EnquiryRepository enquiryRepository;

    public void saveReplies(Long enquiryId, String message)
    {
        Enquiry  enquiry = enquiryRepository.findById(enquiryId).orElseThrow(() -> new RuntimeException("Enquiry not found"));;
        AgentReplies agentReplies1 = new AgentReplies();
        agentReplies1.setEnquiry(enquiry);
        agentReplies1.setMessage(message);
        agentReplies1.setSentAt(LocalDateTime.now());
        replyRepository.save(agentReplies1);
    }


}
