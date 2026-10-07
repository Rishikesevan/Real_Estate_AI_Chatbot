package com.project.RealEstate.Service;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Enquiry;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Repository.EnquiryRepository;
import com.project.RealEstate.Repository.PropertyRepository;
import com.project.RealEstate.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EnquiryService {
    @Autowired
    private EnquiryRepository enquiryRepository;

    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    public void enquiry(Long propertyId, Long agentId, Long userId, String message) {
        if (userId == null) {
            throw new RuntimeException("User must be logged in");
        }

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        User user = userRepository.findById((long) Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Enquiry enquiry = new Enquiry();
        enquiry.setAgent(agent);
        enquiry.setUser(user);
        enquiry.setMessage(message);
        enquiry.setProperty(property);
        enquiryRepository.save(enquiry);
    }

    public List<Enquiry> allEnquires(Long agentId) {
        return enquiryRepository.findByAgentId(agentId);
    }
}
