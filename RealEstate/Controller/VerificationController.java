package com.project.RealEstate.Controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Repository.OwnerRepository;
import com.project.RealEstate.Repository.UserRepository;
import com.project.RealEstate.Service.EmailService;

@Controller
public class VerificationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AgentRepository agentRepository;

    @GetMapping("/verify")
    public String verify(@RequestParam String token, Model model) {

        User user = userRepository.findByToken(token);
        Owner owner = ownerRepository.findByToken(token);
        Agent agent = agentRepository.findByToken(token);

        // 🔹 If neither found
        if (user == null && owner == null && agent == null) {
            model.addAttribute("token", token);
            return "InvalidToken";
        }

        // 🔹 If User exists
        if (user != null) {

            if (user.isEnabled()) {
                return "AlreadyVerified";
            }

            if (user.getTokenExpiryTime() == null ||
                    user.getTokenExpiryTime().isBefore(LocalDateTime.now())) {

                model.addAttribute("token", token);
                return "InvalidToken";
            }

            user.setEnabled(true);
            user.setToken(null);
            user.setTokenExpiryTime(null);
            userRepository.save(user);

            return "VerifiedEmailSuccess";
        }

        // 🔹 If Owner exists
        if (owner != null) {

            if (owner.isEnabled()) {
                return "AlreadyVerified";
            }

            if (owner.getTokenExpiryTime() == null ||
                    owner.getTokenExpiryTime().isBefore(LocalDateTime.now())) {

                model.addAttribute("token", token);
                return "InvalidToken";
            }

            owner.setEnabled(true);
            owner.setToken(null);
            owner.setTokenExpiryTime(null);
            ownerRepository.save(owner);

            return "VerifiedEmailSuccess";
        }

        // If Agent exists
        if(agent!=null)
        {
            if(agent.isEnabled())
            {
                return "AlreadyVerified";
            }
            if(agent.getTokenExpiryTime() == null || 
                agent.getTokenExpiryTime().isBefore(LocalDateTime.now()))
                {
                    model.addAttribute("token", token);
                    return "InvalidToken";
                }
                agent.setEnabled(true);
                agent.setToken(null);
                agent.setTokenExpiryTime(null);
                agentRepository.save(agent);
                return "VerifiedEmailSuccess";

        }

        return "InvalidToken";
    }

    @PostMapping("/resend-verification")
    @ResponseBody
    public String resendVerification(@RequestParam("email") String email) {

        if (email == null || email.trim().isEmpty()) {
            return "Email missing. Please try again.";
        }

        // String trimmedEmail = email.trim();

        Optional<User> optionalUser = userRepository.getUserByEmail(email);
        Optional<Owner> optionalOwner = ownerRepository.getOwnerByEmail(email);
        Optional<Agent> optionalAgent = agentRepository.getUserByEmail(email);

        // If neither exists
        if (optionalUser.isEmpty() && optionalOwner.isEmpty() && optionalAgent.isEmpty()) {
            return "Email not found";
        }

        // 🔹 If User exists
        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (user.isEnabled()) {
                return "Account already verified";
            }

            String newToken = UUID.randomUUID().toString();
            user.setToken(newToken);
            user.setTokenExpiryTime(LocalDateTime.now().plusMinutes(1));

            userRepository.save(user);
            emailService.sendVerificationEmail(user.getEmail(), newToken);

            return "Verification link sent successfully!";
        }

        // 🔹 If Owner exists
        if (optionalOwner.isPresent()) {

            Owner owner = optionalOwner.get();

            if (owner.isEnabled()) {
                return "Account already verified";
            }

            String newToken = UUID.randomUUID().toString();
            owner.setToken(newToken);
            owner.setTokenExpiryTime(LocalDateTime.now().plusMinutes(1));

            ownerRepository.save(owner);
            emailService.sendVerificationEmail(owner.getEmail(), newToken);

            return "Verification link sent successfully!";
        }

        // 🔹 If Agent exists
        if (optionalAgent.isPresent()) {

            Agent agent = optionalAgent.get();

            if (agent.isEnabled()) {
                return "Account already verified";
            }

            String newToken = UUID.randomUUID().toString();
            agent.setToken(newToken);
            agent.setTokenExpiryTime(LocalDateTime.now().plusMinutes(1));

            agentRepository.save(agent);
            emailService.sendVerificationEmail(agent.getEmail(), newToken);

            return "Verification link sent successfully!";
        }

        return "Something went wrong";
    }
}
