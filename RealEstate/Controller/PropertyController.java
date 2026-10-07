package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Service.*;
import jakarta.servlet.http.HttpSession;
import com.project.RealEstate.Repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class PropertyController {
    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserService userService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private OwnerSubscriptionService ownerSubscriptionService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @GetMapping("/api/viewAllProperties")
    public String viewAllProperties(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        
        Set<Long> wishlistPropertyIds = new HashSet<>();
        if (userId != null) {
            wishlistPropertyIds = wishlistRepository.findByUserId(userId)
                    .stream()
                    .map(w -> w.getProperty().getId())
                    .collect(Collectors.toSet());
        }
        model.addAttribute("wishlistPropertyIds", wishlistPropertyIds);

        List<Property> propertyList = propertyService.viewAllProperties();
        List<Property> cproperty = new ArrayList<>();
        for (Property property : propertyList) {
            if (property.getStatus().equals("ACTIVE")) {
                cproperty.add(property);
            }
        }
        model.addAttribute("propertyList", cproperty);
        model.addAttribute("user", user);
        return "AllProperties";
    }

    @GetMapping("/api/viewAllProperties/searchProperty")
    @ResponseBody
    public List<Property> viewPropertiesBySearch(@RequestParam(required = false) String location,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) String bhk,
            Model model) {

        return propertyService.viewPropertiesBySearch(location, propertyType, bhk);
    }

    @GetMapping("/api/property/image/{id}")
    @ResponseBody
    public byte[] getPropertyImage(@PathVariable Long id) {
        Property property = propertyService.getPropertyById(id);
        return property.getImage();
    }

    @GetMapping("/api/viewAllProperties/details/{id}")
    public String viewPropertiesDetails(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        Property property = propertyService.viewPropertiesDetails(id);
        model.addAttribute("property", property);
        model.addAttribute("user", user);
        return "PropertyDetails";
    }

    @GetMapping("/api/owner/viewAllProperties/details/{id}")
    public String viewOwnerPropertiesDetails(@PathVariable Long id, Model model, HttpSession session) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        Owner owner = userService.getOwnerById(ownerId);
        Property property = propertyService.viewPropertiesDetails(id);
        model.addAttribute("property", property);
        model.addAttribute("owner", owner);
        return "PropertyDetails";
    }

    @GetMapping("/api/agent/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 💣 clears userId
        return "redirect:/api/homeIndex";
    }

    @GetMapping("/api/save/property")
    public String saveProperty(Model model, HttpSession session) {

        Long ownerId = (Long) session.getAttribute("ownerId");
        Owner owner = ownerService.getOwnerById(ownerId);
        model.addAttribute("owner", owner);

        String toCheck = ownerSubscriptionService.toCheckSubscription(ownerId);

        if ("ACTIVE".equals(toCheck)) {
            model.addAttribute("propertyData", new Property());
            List<Agent> agentList = agentService.agentList();
            model.addAttribute("agentList", agentList);
            return "PostMyProperty";
        }

        return "Subscription";
    }

    @PostMapping("/api/save")
    public String save(@ModelAttribute("propertyData") Property propertyData,
            @RequestParam("imageFile") MultipartFile multipartFile,
            HttpSession ownerHttpSession,
            @RequestParam(value = "agentId", required = false) Long agentId) throws IOException {
        Property property1 = propertyData;
        propertyData.setImage(multipartFile.getBytes());
        property1.setStatus("ACTIVE");
        Long ownerId = (Long) ownerHttpSession.getAttribute("ownerId");
        Owner owner = ownerService.ownerId(ownerId);
        property1.setOwner(owner);
        if (agentId != null) {
            Agent agent = agentService.viewById(agentId); // must be DB entity
            property1.setAgent(agent);
        } else {
            property1.setAgent(null); // saved as NULL in DB
        }
        propertyService.savePropertyDetails(property1);
        ownerSubscriptionService.editSubscription(ownerId);
        return "redirect:/api/save/property?success";
    }

}
