package com.project.RealEstate.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Entity.WishList;
import com.project.RealEstate.Repository.WishlistRepository;
import com.project.RealEstate.Service.UserService;
import com.project.RealEstate.Service.WishlistService;

import jakarta.servlet.http.HttpSession;

@Controller
public class WishlistController
{
    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private WishlistRepository wishlistRepository;


    @Autowired
    private UserService userService;
    
    // ✅ LOAD PAGE WITH WISHLIST STATE
    @GetMapping("/properties")
    public String getProperties(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        // System.out.println("user id is 1: " + userId);

        Set<Long> wishlistPropertyIds = new HashSet<>();

        if (userId != null) {
            wishlistPropertyIds = wishlistRepository.findByUserId(userId)
                    .stream()
                    .map(w -> w.getProperty().getId())
                    .collect(Collectors.toSet());
        }

        // 👉 also add your properties list here
        // model.addAttribute("properties", propertyService.getAll());

        model.addAttribute("wishlistPropertyIds", wishlistPropertyIds);

        return "properties"; // your thymeleaf page
    }

    // ✅ TOGGLE API (NO REDIRECT)
    @PostMapping("/wishlist/{propertyId}")
    @ResponseBody
    public Map<String, String> toggleWishlist(@PathVariable Long propertyId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        System.out.println("user id is 2: " + userId);

        Map<String, String> response = new HashMap<>();

        if (userId == null) {
            response.put("status", "error");
            response.put("message", "NOT_LOGGED_IN");
            return response;
        }

        String result = wishlistService.toggleWishlist(userId, propertyId);

        response.put("status", "success");
        response.put("action", result); // ADDED / REMOVED

        return response;
    }

    @GetMapping("/api/view/wishlist")
    public String viewWishlist(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/api/login";
        }
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);

        List<WishList> wishlist = wishlistService.findByUserId(userId);
        if(wishlist.isEmpty()) {
            model.addAttribute("message", "Your wishlist is empty");
            return "wishlist";
        }
        model.addAttribute("wishlist", wishlist);
        return "wishlist";
    }

    @GetMapping("/api/view/wishlist/search")
    @ResponseBody
    public List<Property> searchWishlist(@RequestParam(required = false) String location,
                                       @RequestParam(required = false) String propertyType,
                                       @RequestParam(required = false) String bhk,
                                       HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        System.out.println("user id is 4: " + userId);
        if (userId == null) {
            return new ArrayList<>();
        }
        
        List<WishList> filteredWishlist = wishlistService.filterWishlist(userId, location, propertyType, bhk);
        return filteredWishlist.stream()
                .map(WishList::getProperty)
                .collect(Collectors.toList());
    }
}
