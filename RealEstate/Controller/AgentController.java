package com.project.RealEstate.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Enquiry;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.AgentSubscriptionRepository;
import com.project.RealEstate.Service.AgentService;
import com.project.RealEstate.Service.EnquiryService;
import com.project.RealEstate.Service.PropertyService;
import com.project.RealEstate.Service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AgentController {
    @Autowired
    private AgentService agentService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private AgentSubscriptionRepository subscriptionRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/api/showAgent")
    public String showAgent(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        List<Agent> agentList = agentService.showAgent();
        model.addAttribute("agentList", agentList);
        model.addAttribute("user", user);
        return "agent";
    }

    @GetMapping("/api/agent/image/{id}")
    @ResponseBody
    public byte[] getAgentImage(@PathVariable Long id) {
        Agent agent = agentService.getAgentById(id);
        return agent.getImage();
    }

    @GetMapping("/api/searchAgents")
    @ResponseBody
    public List<Agent> searchAgents(@RequestParam String keyword) {
        return agentService.searchAgents(keyword);
    }

    @GetMapping("/api/showAgent/profile/{id}")
    public String showAgentProfile(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        Agent agentList = agentService.showAgentProfile(id);
        List<Property> Properties = agentService.findByPropertiesOfAgent(id);
        model.addAttribute("agent", agentList);
        model.addAttribute("properties", Properties);
        model.addAttribute("user", user);
        return "AgentProfile";
    }

    @GetMapping("/api/owner/showAgent/profile/{id}")
    public String showOwnerAgentProfile(@PathVariable Long id, Model model, HttpSession session) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        Owner owner = userService.getOwnerById(ownerId);
        Agent agentList = agentService.showAgentProfile(id);
        List<Property> Properties = agentService.findByPropertiesOfAgent(id);
        model.addAttribute("agent", agentList);
        model.addAttribute("properties", Properties);
        model.addAttribute("owner", owner);
        return "AgentProfile";
    }

    @GetMapping("/api/agentIndex")
    public String showAgentIndex(HttpSession session, Model model) {
        Long agentId = (Long) session.getAttribute("agentId");
        if(agentId==null){
            return "redirect:/api/login";
        }
        Agent agent = agentService.getAgentById(agentId);
        List<Enquiry> enquiries = enquiryService.allEnquires(agentId);
        List<Property> Properties = agentService.findByPropertiesOfAgent(agentId);
        List<Property> agentProperties = propertyService.getLimitedPropertiesByAgent(agentId, 4);
        model.addAttribute("agent", agent);
        boolean hasPaid = subscriptionRepository
                .existsByAgentIdAndActiveTrue(agentId);

        model.addAttribute("hasPaid", hasPaid);
        model.addAttribute("agentId", agentId);
        model.addAttribute("enquiries", enquiries);
        System.out.println("ENQUIRIES COUNT = " + enquiries.size());

        model.addAttribute("properties", Properties);
        model.addAttribute("agentProperties", agentProperties);
        return "AgentIndex";
    }

    @GetMapping("/api/agent/enquiries")
    @ResponseBody
    public List<Map<String, Object>> getEnquiries(HttpSession session) {
        Long agentId = (Long) session.getAttribute("agentId");
        List<Enquiry> enquiries = enquiryService.allEnquires(agentId);

        // Transform Enquiry to simple Map
        List<Map<String, Object>> result = enquiries.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("name", e.getUser().getName());
            map.put("email", e.getUser().getEmail());
            map.put("message", e.getMessage());

            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put("id", e.getProperty().getId());
            propertyMap.put("title", e.getProperty().getTitle());
            propertyMap.put("location", e.getProperty().getLocation());
            propertyMap.put("image", e.getProperty().getImage() != null); // boolean if image exists

            map.put("property", propertyMap);
            return map;
        }).toList();

        return result;
    }

    @GetMapping("/api/agent/agentProperties")
    public String showAgentProperties(HttpSession session, Model model) {
        Long agentId = (Long) session.getAttribute("agentId");
        List<Property> agentProperties = propertyService.findByPropertiesOfAgent(agentId);
        List<Enquiry> enquiries = enquiryService.allEnquires(agentId);
        Agent agent = agentService.getAgentById(agentId);
        List<Property> soldProperty = new ArrayList<>();
        List<Property> rentProperty = new ArrayList<>();
        List<Property> removeProperty = new ArrayList<>();
        for (Property property : agentProperties) {
            if (property.getStatus().equals("SOLD")) {
                soldProperty.add(property);
            }
            if (property.getStatus().equals("RENT")) {
                rentProperty.add(property);
            }
            if (property.getStatus().equals("REMOVE")) {
                removeProperty.add(property);
            }
        }
        model.addAttribute("soldProperty", soldProperty);
        model.addAttribute("rentProperty", rentProperty);
        model.addAttribute("removeProperty", removeProperty);
        model.addAttribute("agentProperties", agentProperties);
        model.addAttribute("enquiries", enquiries);
        model.addAttribute("agent", agent);
        return "AgentProperties";

    }

    @GetMapping("/api/save/ediprofile")
    public String saveNewProperty(HttpSession session, Model model) {
        Long agentId = (Long) session.getAttribute("agentId");
        Agent agent = agentService.getAgentById(agentId);
        model.addAttribute("agent", agent);
        return "EditAgentProfile";
    }

    @PostMapping("/api/agent/updateprofile")
    public String updateAgentProfile(@ModelAttribute Agent agent,
            @RequestParam("imageFile") MultipartFile image)
            throws IOException {

        if (agent.getName() == null) {
            return "redirect:/api/agentIndex";
        }

        agentService.updateAgentProfile(agent, image);

        return "redirect:/api/agentIndex?success";
    }

    @GetMapping("/api/agent/showsignup")
    public String showSignup(Model model) {
        model.addAttribute("agent", new Agent());
        return "AgentSignUp";
    }

    @PostMapping("/api/agent/agentsignup")
    public String agentSignup(@Valid @ModelAttribute("agent") Agent agent,BindingResult result, @RequestParam("imageFile") MultipartFile image, Model model)
            throws IOException {
        if (agentService.emailExists(agent.getEmail())) {
            result.rejectValue("email", "error.agent", "Email already exists");
        }
        if (agentService.phoneExists(agent.getPhone())) {
            result.rejectValue("phone", "error.agent", "Phone Number already exists");
        }
        if(agentService.licenseNumberExists(agent.getLicenseNumber())){
            result.rejectValue("licenseNumber", "error.agent", "License Number already exists");
        }
        if (result.hasErrors()) {
            return "AgentSignUp";
        }
        
        if (!image.isEmpty()) {
            agent.setImage(image.getBytes());
        }

        Agent agent1 = agentService.agentSignup(agent);
        model.addAttribute("agent", agent1);
        return "redirect:/api/check-email?email=" + agent.getEmail();
    }

    // @GetMapping("/api/check-email")
    // public String checkEmail(@RequestParam("email") String email, Model model) {
    //     model.addAttribute("email", email);
    //     return "CheckEmail";
    // }

}
