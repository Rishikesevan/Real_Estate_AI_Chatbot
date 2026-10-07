package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Service.NewProjectService;
import com.project.RealEstate.Service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {
	@Autowired
	private NewProjectService newProjectService;

	@Autowired
	private PropertyService propertyService;

	@GetMapping("/")
	public String home(Model model) {
		List<NewProject> newProject = newProjectService.home();
		List<Property> properties = propertyService.getHomeProperties();
		model.addAttribute("viewAll", properties);
		model.addAttribute("newProject", newProject);
		return "HomePage";
	}

	@GetMapping("/api/agent/response")
	public String agentResponse(Model model) {
		return "AgentResponse";
	}

	@GetMapping("/api/allsignup")
	public String allSignup(Model model) {
		return "CreateAccount";
	}

}
