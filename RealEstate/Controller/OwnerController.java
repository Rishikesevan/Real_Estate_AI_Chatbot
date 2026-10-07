package com.project.RealEstate.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Service.AgentService;
import com.project.RealEstate.Service.NewProjectService;
import com.project.RealEstate.Service.OwnerService;
import com.project.RealEstate.Service.PropertyService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class OwnerController {
    @Autowired
    private OwnerService ownerService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private NewProjectService newProjectService;

    @Autowired
    private AgentService agentService;

    @GetMapping("/owner/homepage")
    public String ownerHomepage(Model model, HttpSession session) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        if(ownerId==null){
            return "redirect:/api/login";
        }

        List<Property> properties = propertyService.viewAllProperty();
        int active = 0;

        int soldProperty = 0;
        int newProperty = 0;
        Property property1 = new Property();
        for (Property property : properties) {
            if (property.getOwner().getId().equals(ownerId)) {
                property1 = property;
                if (property.getStatus().toLowerCase().equals("sold")) {
                    soldProperty++;
                } else if (property.getStatus().toLowerCase().equals("active")) {
                    active++;
                }
            }
        }
        List<NewProject> newProjectList = newProjectService.newProjectList();
        for (NewProject newProject : newProjectList) {
            if (newProject.getOwner().getId().equals(ownerId)) {
                newProperty++;
            }
        }
        String ownername = ownerService.ownerName(ownerId);

        int all = newProperty + active + soldProperty;
        model.addAttribute("active", active);
        model.addAttribute("sold", soldProperty);
        model.addAttribute("newProject", newProperty);
        model.addAttribute("allProperty", all);
        model.addAttribute("lastProperty", property1);
        model.addAttribute("ownerName", ownername);
        return "OwnerIndex";
    }

    @GetMapping("/api/owner/showAgent")
    public String showAgentForOwner(Model model, HttpSession session) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        Owner owner = ownerService.getOwnerById(ownerId);
        List<Agent> agentList = agentService.showAgent();
        model.addAttribute("agentList", agentList);
        model.addAttribute("owner", owner);
        return "agent";
    }

    @GetMapping("/api/owner/signup")
    public String ownerSignup(Model model) {
        Owner owner = new Owner();
        model.addAttribute("ownerSignup", owner);
        return "ownerSignup";
    }

    @PostMapping("/api/owner/save")
    public String saveOwner(@Valid @ModelAttribute("ownerSignup") Owner ownerSignup, BindingResult result,
            Model model) {
        if (ownerService.emailExists(ownerSignup.getEmail())) {
            result.rejectValue("email", "error.owner", "Email already exists");
        }
        if (ownerService.phoneExists(ownerSignup.getPhone())) {
            result.rejectValue("phone", "error.owner", "Phone number already exists");
        }
        if (result.hasErrors()) {
            return "ownerSignup";
        }
        ownerService.saveOwner(ownerSignup);
        model.addAttribute("ownerSignup", new Owner());
        return "redirect:/api/check-email?email=" + ownerSignup.getEmail();
    }

    @GetMapping("/api/save/editownerprofile")
    public String showUpdate(HttpSession session, Model model) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        Owner owner = ownerService.getOwnerById(ownerId);
        model.addAttribute("owner", owner);
        return "EditOwnerProfile";
    }

    @PostMapping("/api/owner/updateprofile")
    public String updateProfile(@ModelAttribute Owner owner, Model model) {
        Owner owner1 = ownerService.updateOwnerProfile(owner);
        model.addAttribute("owner", owner1);
        return "redirect:/owner/homepage?success";
    }

}
