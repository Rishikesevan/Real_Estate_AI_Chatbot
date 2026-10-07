package com.project.RealEstate.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Repository.PropertyRepository;

@Service
public class AgentService
{
    @Autowired
    private AgentRepository agentRepository;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private PropertyRepository propertyRepository;


    public List<Agent> showAgent()
    {
        return  agentRepository.findAll();
    }

    public Agent getAgentById(Long id) {
        return agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));
    }


    public List<Agent> searchAgents(String keyword)
    {
        if(keyword == null || keyword.trim().isEmpty())
        {
            return agentRepository.findAll();
        }
        return agentRepository.searchAgents(keyword);
    }

    public Agent showAgentProfile(Long id)
    {
        return agentRepository.findById(id).orElseThrow(() -> new RuntimeException("Agent not found"));
    }

    public List<Property> findByPropertiesOfAgent(Long id) {
        List<Property> propertyAgent = propertyRepository.findByAgentId(id);
        if (propertyAgent == null) {
            return new ArrayList<>();
        }
        return propertyAgent;
    }

    public Agent updateAgentProfile(Agent agent, MultipartFile image) throws IOException {

        Agent existingAgent = agentRepository.getAgentById(agent.getId());

        if (existingAgent == null) {
            throw new RuntimeException("Agent not found");
        }

        existingAgent.setName(agent.getName());
        existingAgent.setLicenseNumber(agent.getLicenseNumber());
        existingAgent.setCompanyName(agent.getCompanyName());
        existingAgent.setPhone(agent.getPhone());

        if (agent.getPassword() != null && !agent.getPassword().isEmpty()) {
            existingAgent.setPassword(agent.getPassword());
        }

        if (image != null && !image.isEmpty()) {
            existingAgent.setImage(image.getBytes());
        }

        return agentRepository.save(existingAgent);
    }

    public Agent viewById(Long id){
        return agentRepository.findById(id).orElse(null);
    }

    public Agent agentSignup(Agent agent)
    {
        agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        agent.setEnabled(false);

        String token = UUID.randomUUID().toString();
        agent.setToken(token);
        agent.setTokenExpiryTime(LocalDateTime.now().plusSeconds(30));
        Agent agent1 = agentRepository.save(agent);
        emailService.sendVerificationEmail(agent.getEmail(), token);
        return agent1;
    }

    public boolean emailExists(String email) {
        return agentRepository.existsByEmail(email);
    }

    public boolean phoneExists(String phone) {
        return agentRepository.existsByPhone(phone);
    }
    public boolean licenseNumberExists(String licenseNumber) {
        return agentRepository.existsByLicenseNumber(licenseNumber);
    }

    public List<Agent> agentList (){
        return agentRepository.findAll();
    }
}
