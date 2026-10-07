package com.project.RealEstate.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Repository.OwnerRepository;

@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Owner saveOwner(Owner owner) {
        owner.setPassword(passwordEncoder.encode(owner.getPassword()));
        owner.setEnabled(false);
        String token = UUID.randomUUID().toString();
        owner.setToken(token);
        owner.setTokenExpiryTime(LocalDateTime.now().plusSeconds(30));
        Owner owner1 = ownerRepository.save(owner);
        emailService.sendVerificationEmail(owner.getEmail(), token);
        return owner1;
    }

    public boolean emailExists(String email) {
        return ownerRepository.existsByEmail(email);
    }

    public boolean phoneExists(String phone) {
        return ownerRepository.existsByPhone(phone);
    }

    public List<Owner> viewAll() {
        return ownerRepository.findAll();
    }

    public Owner ownerId(Long Id) {
        Owner owner = ownerRepository.findById(Id).orElse(null);
        return owner;
    }

    public Owner findbyemails(String email, String password) {
        List<Owner> owners = ownerRepository.findAll();
        for (Owner owner : owners) {
            if (owner.getEmail().equals(email) && owner.getPassword().equals(password)) {
                return owner;
            }
        }
        return null;
    }

    public String ownerName(Long id) {
        Owner owner = ownerRepository.findById(id).orElse(null);
        String ownerName = owner.getName();

        return ownerName;
    }

    public Owner getOwnerById(Long ownerId) {
        List<Owner> ownerList = ownerRepository.findAll();
        for (Owner owner : ownerList) {
            if (owner.getId().equals(ownerId)) {
                return owner;
            }
        }
        return null;
    }

    public Owner updateOwnerProfile(Owner owner) {
        Owner owner1 = ownerRepository.findById(owner.getId()).get();
        if (owner1 == null) {
            throw new RuntimeException("owner not found");
        }
        owner1.setName(owner.getName());
        owner1.setEmail(owner.getEmail());
        owner1.setPhone(owner.getPhone());
        owner1.setAddress(owner.getAddress());
        if (owner.getPassword() != null && !owner.getPassword().isEmpty()) {
            owner1.setPassword(owner.getPassword());
        }
        return ownerRepository.save(owner1);
    }
}
