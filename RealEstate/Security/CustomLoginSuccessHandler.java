package com.project.RealEstate.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.project.RealEstate.Service.OwnerSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OwnerSubscriptionService ownerSubscriptionService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        HttpSession session = request.getSession();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        String redirectUrl = "/api/login?error";

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("ROLE_USER")) {
                session.setAttribute("userId", userDetails.getOriginalId());
                redirectUrl = "/api/userIndex";
                break;
            } else if (authority.getAuthority().equals("ROLE_OWNER")) {
                session.setAttribute("ownerId", userDetails.getOriginalId());
                // Handle the subscription side-effect from the original controller
                ownerSubscriptionService.editPlain(userDetails.getOriginalId());
                redirectUrl = "/owner/homepage";
                break;
            } else if (authority.getAuthority().equals("ROLE_AGENT")) {
                session.setAttribute("agentId", userDetails.getOriginalId());
                redirectUrl = "/api/agentIndex";
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}
