package com.project.RealEstate.Controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Service.NewProjectService;
import com.project.RealEstate.Service.OwnerService;
import com.project.RealEstate.Service.OwnerSubscriptionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class NewProjectController {
    @Autowired
    private NewProjectService newProjectService;

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private OwnerSubscriptionService ownerSubscriptionService;

    @GetMapping("/api/newProject")
    public String addNewProject(Model model, HttpSession session) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        Owner owner = ownerService.getOwnerById(ownerId);
        model.addAttribute("owner", owner);

        String toCheck = ownerSubscriptionService.toCheckSubscription(ownerId);

        if ("ACTIVE".equals(toCheck)) {
            model.addAttribute("newProject", new NewProject());
            return "NewProject";
        }

        return "Subscription";
    }

    @PostMapping("/api/save/mewProject")
    public String saveNewProject(@ModelAttribute NewProject newProject,
            HttpSession httpSession,
            @RequestParam("imageFile") MultipartFile multipartFile) throws IOException {
        Long ownerId = (Long) httpSession.getAttribute("ownerId");
        Owner owner = ownerService.ownerId(ownerId);
        newProject.setOwner(owner);
        newProject.setImage(multipartFile.getBytes());
        newProjectService.saveNewProject(newProject);
        ownerSubscriptionService.editSubscription(ownerId);
        return "redirect:/api/newProject?success";

    }

    // @GetMapping("/api/view/newProject")
    // public String viewNewProject(Model model, HttpSession session) {
    //     Long userId = (Long) session.getAttribute("userId");
    //     User user = userService.getOwnerById(userId);
    //     model.addAttribute("user", user);
    //     List<NewProject> projects = newProjectService.newProjectList();
    //     model.addAttribute("viewAllProject", projects);
    //     return "ViewNewProject";
    // }

    // edit New Project
    @GetMapping("/api/update/newProject/{nId}")
    public String updateNewProject(@PathVariable("nId") Long nId, Model model) {

        NewProject newProject = newProjectService.viewById(nId);
        model.addAttribute("newProject", newProject);
        return "EditMyNewProject";
    }

    // use putting->>
    @PostMapping("/api/update")
    public String updatedNewProject(@RequestParam(value = "newImageFile", required = false) MultipartFile multipartFile,
            @ModelAttribute NewProject newProject,
            @RequestParam(value = "removeImaged") boolean removeImaged) throws IOException {
        NewProject newProject1 = newProjectService.viewById(newProject.getId());
        if (removeImaged) {
            newProject1.setImage(null);
        } else if (!multipartFile.isEmpty() && !removeImaged) {
            newProject1.setImage(multipartFile.getBytes());
        }

        newProjectService.updateNewProject(newProject);

        return "redirect:/api/myProperty";
    }

    // delete->
    @GetMapping("/api/newProject/delete/{dId}")
    public String deleteNewProject(@PathVariable("dId") Long dId) {
        newProjectService.deleteNewProject(dId);
        return "redirect:/api/myProperty";
    }

    @GetMapping("/api/project-image/{id}")
    public ResponseEntity<byte[]> getProjectImage(@PathVariable Long id) {

        NewProject project = newProjectService.findById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(project.getImage());
    }

}
