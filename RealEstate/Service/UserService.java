package com.project.RealEstate.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.OwnerRepository;
import com.project.RealEstate.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public User signUp(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        String token = UUID.randomUUID().toString();
        user.setToken(token);
        user.setTokenExpiryTime(LocalDateTime.now().plusMinutes(5));
        User user1 = userRepository.save(user);
        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
        } catch (Exception e) {
            System.out.println("---------UserService Exception:---------- " + e.getMessage());
            // e.printStackTrace();
        }
        return user1;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    public User getUserById(Long userId) {
        List<User> userList = userRepository.findAll();
        for (User user : userList) {
            if (user.getId().equals(userId)) {
                // System.out.println(user.getName());
                return user;
            }
        }

        return null;
    }

    public Owner getOwnerById(Long ownerId) {
        List<Owner> ownerList = ownerRepository.findAll();
        for (Owner owner : ownerList) {
            if (owner.getId().equals(ownerId)) {
                // System.out.println(user.getName());
                return owner;
            }
        }

        return null;
    }

    public User updateUserProfile(User user) {
        User user1 = userRepository.findById(user.getId()).get();
        if (user1 == null) {
            throw new RuntimeException("User not found");
        }
        user1.setName(user.getName());
        user1.setEmail(user.getEmail());
        user1.setPhone(user.getPhone());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user1.setPassword(user.getPassword());
        }
        return userRepository.save(user1);
    }

}
