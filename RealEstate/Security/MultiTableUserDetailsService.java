package com.project.RealEstate.Security;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.Owner;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Repository.OwnerRepository;
import com.project.RealEstate.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MultiTableUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Check User Table
        List<User> users = userRepository.findByEmail(email);
        for (User user : users) {
            // In a real app we would check password here too, but Spring Security does that
            // later.
            // However, since we have duplicate emails possible across tables, we return the
            // first match.
            // NOTE: Standard Spring Security UserDetailsService only returns UserDetails
            // based on username.
            // The AuthenticationProvider checks the password.
            // Since we are using standard DAO authentication, we return the user details
            // and let the provider check the password.
            return new CustomUserDetails(
                    user.getEmail(),
                    user.getPassword(),
                    user.getId(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        }

        // 2. Check Owner Table
        List<Owner> owners = ownerRepository.findByEmail(email);
        for (Owner owner : owners) {
            // Trigger subscription check side-effect from original controller
            // Note: ideally this should be in an event listener or login success handler,
            // but to preserve exact logic we can do it here
            // or move it to success handler. Moving to SuccessHandler is cleaner but let's
            // stick to the flow.
            // Actually, side effects in loadUserByUsername are bad. We should move the
            // subscription check to SuccessHandler or leave it.
            // Original: ownerSubscriptionService.editPlain(o.getId());
            // We will handle this in the Success Handler if possible, or just accept that
            // we need to do it here?
            // No, loadUserByUsername is called during auth. SuccessHandler is better.

            return new CustomUserDetails(
                    owner.getEmail(),
                    owner.getPassword(),
                    owner.getId(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_OWNER")));
        }

        // 3. Check Agent Table
        List<Agent> agents = agentRepository.findByEmail(email);
        for (Agent agent : agents) {
            return new CustomUserDetails(
                    agent.getEmail(),
                    agent.getPassword(),
                    agent.getId(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")));
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
