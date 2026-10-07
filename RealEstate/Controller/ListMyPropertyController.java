package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.Transaction;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Service.ListMyPropertyService;
import com.project.RealEstate.Service.NewProjectService;
import com.project.RealEstate.Service.OwnerService;
import com.project.RealEstate.Service.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ListMyPropertyController {

    @Autowired
    private ListMyPropertyService listMyPropertyService;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private NewProjectService newProjectService;

    @Autowired
    private OwnerService ownerService;

    @GetMapping("/api/myProperty")
    public String myProperty(Model model, HttpSession httpSession) {
        Long ownerId = (Long) httpSession.getAttribute("ownerId");
        Owner owner = ownerService.getOwnerById(ownerId);
        model.addAttribute("owner", owner);

        List<Property> properties = listMyPropertyService.myProperty(ownerId);
        int active = 0;
        int sold = 0;
        for (Property property : properties) {
            String pStatus = property.getStatus().toLowerCase();
            if (pStatus.equals("active")) {
                active++;
            } else if (pStatus.equals("sold")) {
                sold++;
            }
        }
        List<NewProject> newProjectList = newProjectService.newProjectList();

        List<NewProject> newProjectList1 = new ArrayList<>();
        for (NewProject newProject : newProjectList) {
            if (newProject.getOwner().getId().equals(ownerId)) {
                newProjectList1.add(newProject);
            }
        }
        model.addAttribute("activeCount", active);
        model.addAttribute("SoldCount", sold);
        model.addAttribute("myProperty", properties);
        model.addAttribute("newProjectList", newProjectList1);
        return "MyProperty";
    }

    @GetMapping("/api/edit/{id}")
    public String editMyproperty(@PathVariable("id") Long id, Model model) {
        Property property = listMyPropertyService.view(id);

        model.addAttribute("editMyProperty", property);
        List<Agent> agentList = agentRepository.findAll();
        model.addAttribute("agentList", agentList);
        // ✅ SAFE values for UI
        model.addAttribute("agentName",
                property.getAgent() != null ? property.getAgent().getName() : null);

        model.addAttribute("agentId",
                property.getAgent() != null ? property.getAgent().getId() : null);

        return "EditMyProperty";
    }

    // putmapping
    @PostMapping("/api/update/property")
    public String updateProperty(@ModelAttribute Property property,
            @RequestParam(value = "imageFile", required = false) MultipartFile multipartFile,
            @RequestParam(value = "soldDate", required = false) LocalDate soldDate,
            @RequestParam(value = "soldPrice", required = false) Double soldPrice,
            @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage) throws IOException {
        Property property1 = listMyPropertyService.view(property.getId());
        if (removeImage) {
            property1.setImage(null);
        } else if (!removeImage && !multipartFile.isEmpty()) {
            property1.setImage(multipartFile.getBytes());
        }

        if (property.getAgent() != null && property.getAgent().getId() == null) {
            property.setAgent(null);
        }
        listMyPropertyService.update(property, soldPrice, soldDate);

        return "redirect:/api/myProperty";
    }

    // delete-> to change delete mapping
    @GetMapping("/api/delete/{ids}")
    public String deleteById(@PathVariable("ids") Long ids) {
        listMyPropertyService.deleteById(ids);
        return "redirect:/api/myProperty";
    }

    @GetMapping("/api/view/sold/{pid}/{type}")
    public String viewSoldProperty(@PathVariable("pid") Long pid,
            @PathVariable("type") String type, Model model) {
        Property property = listMyPropertyService.viewSoldProperty(pid);
        model.addAttribute("soldProperty", property);
        Transaction transaction = transactionService.viewTransaction(pid);
        model.addAttribute("soldTransaction", transaction);
        model.addAttribute("type", type);
        return "ViewSoldProperty";
    }

}
