package com.project.RealEstate.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.RealEstate.Entity.Property;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Entity.WishList;
import com.project.RealEstate.Repository.PropertyRepository;
import com.project.RealEstate.Repository.UserRepository;
import com.project.RealEstate.Repository.WishlistRepository;

@Service
public class WishlistService 
{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private WishlistRepository wishlistRepository;
 
    public String toggleWishlist(Long userId, Long propertyId) {
        System.out.println("user id is Service 1: " + userId);
        System.out.println("property id is Service 1: " + propertyId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        Optional<WishList> existing =
                wishlistRepository.findByUserIdAndPropertyId(userId, propertyId);

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return "REMOVED";
        } else {
            WishList wishList = new WishList();
            wishList.setUser(user);
            wishList.setProperty(property);
            wishlistRepository.save(wishList);
            return "ADDED";
        }
    }    
    public List<WishList> findByUserId(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    public List<WishList> filterWishlist(Long userId, String location, String propertyType, String bhk) {
        List<WishList> wishlist = wishlistRepository.findByUserId(userId);

        Integer bhkValue = null;
        if (bhk != null && !bhk.trim().isEmpty()) {
            try {
                bhkValue = Integer.parseInt(bhk);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        Integer finalBhkValue = bhkValue;
        return wishlist.stream()
                .filter(w -> {
                    var p = w.getProperty();
                    boolean matchesLocation = location == null || location.trim().isEmpty()
                            || p.getLocation().toLowerCase().contains(location.toLowerCase());
                    boolean matchesType = propertyType == null || propertyType.trim().isEmpty()
                            || p.getPropertyType().equalsIgnoreCase(propertyType);
                    boolean matchesBhk = finalBhkValue == null || p.getBhk() == finalBhkValue;
                    return matchesLocation && matchesType && matchesBhk;
                })
                .collect(Collectors.toList());
    }
}
