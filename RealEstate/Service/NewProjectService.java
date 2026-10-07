package com.project.RealEstate.Service;

import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Repository.NewProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NewProjectService
{
    @Autowired
    private NewProjectRepository newProjectRepository;

    public NewProject saveNewProject(NewProject newProject){
        return newProjectRepository.save(newProject);
    }

    public List<NewProject> newProjectList(){
        return newProjectRepository.findAll();
    }

    public NewProject viewById(Long Id){
        NewProject newProject = newProjectRepository.findById(Id).orElse(null);

        return newProject;
    }

    public NewProject updateNewProject(NewProject newProject){
        NewProject newProject1 = newProjectRepository.findById(newProject.getId()).orElse(null);

        newProject1.setProjectName(newProject.getProjectName());
        newProject1.setBuilderName(newProject.getBuilderName());
        newProject1.setHandOverDate(newProject.getHandOverDate());
        newProject1.setStatus(newProject.getStatus());
        newProject1.setStartingPrice(newProject.getStartingPrice());


        newProjectRepository.save(newProject1);

        return newProject;

    }

    public List<NewProject> frontpageNewProjectList(){
        List<NewProject> newProjectList = newProjectRepository.findAll();
        List<NewProject> newProjectList1 = new ArrayList<>();

        for(NewProject newProject:newProjectList){
            newProjectList1.add(newProject);
            if(newProjectList1.size()==6){
                break;
            }
        }

        return newProjectList1;
    }

    public void deleteNewProject(Long id){
        newProjectRepository.deleteById(id);
    }



    public List<NewProject> home()
    {
        List<NewProject> newProject = newProjectRepository.findAll();
        List<NewProject> newProjects = new ArrayList<>();
        for(NewProject newProject1:newProject)
        {
            newProjects.add(newProject1);
        }
        return  newProjects;
    }

    public NewProject findById(Long id) {
        return newProjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }
}
