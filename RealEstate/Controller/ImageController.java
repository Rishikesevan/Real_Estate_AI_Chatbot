package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Service.NewProjectService;
import com.project.RealEstate.Service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ImageController {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private NewProjectService newProjectService;

        @GetMapping("/api/images/{id}")
        public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
            List<Property> proprtyList = propertyService.viewAllProperty();
            Property property = new Property();
            for(Property property1:proprtyList ){
                if(id.equals(property1.getId())) {
                    property = property1;
                }
            }
            if (property.getImage() == null)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(property.getImage());
        }

    @GetMapping("/api/newProject/images/{id}")
    public ResponseEntity<byte[]> getNewProjectImage(@PathVariable Long id) {
        List<NewProject> projectList = newProjectService.newProjectList();
        NewProject newProject = new NewProject();
        for(NewProject newProject1:projectList){
            if(id.equals(newProject1.getId())) {
                newProject=newProject1;
            }
        }
        if (newProject.getImage() == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(newProject.getImage());
    }
}
