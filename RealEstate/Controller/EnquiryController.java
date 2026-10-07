package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Enquiry;
import com.project.RealEstate.Service.AgentService;
import com.project.RealEstate.Service.EnquiryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class EnquiryController {
    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private AgentService agentService;

    @PostMapping("/api/post/enquiry")
    public String enquiry(@RequestParam Long propertyId,
            @RequestParam Long agentId,
            @RequestParam String message,
            HttpSession session) {
        Object sessionUser = session.getAttribute("userId");

        if (sessionUser == null) {
            return "redirect:/api/login?erroritem";
        }

        Long userId = (Long) sessionUser;

        enquiryService.enquiry(propertyId, agentId, userId, message);

        return "redirect:/api/viewAllProperties/details/" + propertyId;
    }

    @GetMapping("/api/agent/enquiriesofusers")
    public String showEnquiriesOfUsers(HttpSession session, Model model) {
        Long agentId = (Long) session.getAttribute("agentId");
        if (agentId == null) {
            return "redirect:/api/login";
        }
        Agent agent = agentService.getAgentById(agentId);
        List<Enquiry> userEnquires = enquiryService.allEnquires(agentId);
        model.addAttribute("userEnquires", userEnquires);
        System.out.println("ENQUIRIES COUNT = " + userEnquires.size());
        model.addAttribute("agent", agent);
        return "AllEnquires"; 
    }

}
