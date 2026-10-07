package com.project.RealEstate.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.NewProject;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Repository.OwnerRepository;
import com.project.RealEstate.Repository.PropertyRepository;
import com.project.RealEstate.Repository.UserRepository;
import com.project.RealEstate.Repository.WishlistRepository;
import com.project.RealEstate.Service.NewProjectService;
import com.project.RealEstate.Service.OwnerSubscriptionService;
import com.project.RealEstate.Service.PropertyService;
import com.project.RealEstate.Service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private NewProjectService newProjectService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private OwnerSubscriptionService ownerSubscriptionService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @GetMapping("/api/login")
    public String login(Model model) {
        model.addAttribute("loginForm", new User());
        return "UserLogin";
    }

    @GetMapping("/api/userIndex")
    public String home(Model model, HttpSession session) {
        Long usedId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(usedId);
        Set<Long> wishlistPropertyIds = new HashSet<>();
        if (usedId != null) {
            wishlistPropertyIds = wishlistRepository.findByUserId(usedId)
                    .stream()
                    .map(w -> w.getProperty().getId())
                    .collect(Collectors.toSet());
        }
        model.addAttribute("wishlistPropertyIds", wishlistPropertyIds);
        List<NewProject> newProject = newProjectService.home();
        List<Property> properties = propertyService.getHomeProperties();
        List<Property> properties2 = new ArrayList<>();
        for (Property property : properties) {
            if (property.getStatus().equals("ACTIVE")) {
                properties2.add(property);
            }
        }
        model.addAttribute("viewAll", properties2);
        model.addAttribute("newProject", newProject);
        model.addAttribute("user", user);
        return "UserIndex";
    }

    @GetMapping("/api/view/newProject")
    public String viewNewProject(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        List<NewProject> projects = newProjectService.newProjectList();
        model.addAttribute("viewAllProject", projects);
        return "ViewNewProject";
    }

    @PostMapping("/api/user/login")
    public String userLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        List<User> user = userRepository.findByEmail(email);
        List<Owner> owner = ownerRepository.findByEmail(email);
        List<Agent> agent = agentRepository.findByEmail(email);

        for (User u : user) {
            if (u.getEmail().equals(email) && u.getPassword().equals(password)) {
                if (!u.isEnabled()) {
                    System.out.println("User is not enabled");
                    return "redirect:/api/check-email?email=" + email + "&error";
                }
                System.out.println("User is enabled");
                session.setAttribute("userId", u.getId());
                return "redirect:/api/userIndex";
            }
        }
        for (Owner o : owner) {
            if (o.getEmail().equals(email) && o.getPassword().equals(password)) {
                session.setAttribute("ownerId", o.getId());
                ownerSubscriptionService.editPlain(o.getId());
                return "redirect:/owner/homepage";
            }
        }
        for (Agent a : agent) {
            if (a.getEmail().equals(email) && a.getPassword().equals(password)) {
                session.setAttribute("agentId", a.getId());
                return "redirect:/api/agentIndex";
            }
        }
        return "redirect:/api/login?error";
    }

    @GetMapping("/api/signup")
    public String signup(Model model) {
        model.addAttribute("signupForm", new User());
        return "UserSignup";
    }

    @PostMapping("/api/user/register")
    public String signUp(@Valid @ModelAttribute("signupForm") User user, BindingResult result, Model model) {
        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "Email already exists");
        }
        if (userService.phoneExists(user.getPhone())) {
            result.rejectValue("phone", "error.user", "Phone number already exists");
        }
        if (result.hasErrors()) {
            return "UserSignup";
        }
        User user1 = userService.signUp(user);
        model.addAttribute("signupForm", user1);
        return "redirect:/api/check-email?email=" + user.getEmail();
    }

    @GetMapping("/api/check-email")
    public String checkEmail(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "CheckEmail";
    }

    @GetMapping("/api/search/userproperty")
    public String searchProperty(@RequestParam("type") String type,
            @RequestParam(value = "query", required = false) String location, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        List<Property> properties = propertyRepository.findAll();
        List<Property> searchProperties = new ArrayList<>();
        for (Property property : properties) {
            boolean matchBuyrent = type != null && property.getBuyRent() != null && property.getBuyRent().equals(type);
            boolean matchStatus = property.getStatus() != null && property.getStatus().equals("ACTIVE");
            boolean matchLocation = (location == null || location.isBlank()) ||
                    (property.getLocation() != null &&
                            property.getLocation().toLowerCase().contains(location.toLowerCase()));
            if (matchBuyrent && matchStatus && matchLocation) {
                searchProperties.add(property);
            }
            if (matchBuyrent && matchStatus && !matchLocation) {
                model.addAttribute("error", "Your Searched Property is not found");
            }
        }
        model.addAttribute("searchProperties", searchProperties);
        model.addAttribute("type", type);
        model.addAttribute("location", location);
        model.addAttribute("user", user);
        return "RentBuySearch";

    }

    @GetMapping("/api/save/edituserprofile")
    public String editProfile(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "EditUserProfile";
    }

    @PostMapping("/api/user/updateprofile")
    public String updateProfile(@ModelAttribute User user, Model model) {
        User user1 = userService.updateUserProfile(user);
        model.addAttribute("user", user1);
        return "redirect:/api/userIndex?success";
    }

    @GetMapping("/api/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
