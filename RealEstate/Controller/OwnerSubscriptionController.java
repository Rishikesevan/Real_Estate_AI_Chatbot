package com.project.RealEstate.Controller;

import com.project.RealEstate.Service.OwnerSubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OwnerSubscriptionController {
    @Autowired
    private OwnerSubscriptionService ownerSubscriptionService;

    @PostMapping("/api/saved/subscription/{orderId}/{amount}")
    public String saveSubscription(@PathVariable("orderId") String orderId, @PathVariable("amount") Integer amount,
            HttpSession session) {
        Long ownerId = (Long) session.getAttribute("ownerId");
        ownerSubscriptionService.saveSubscription(orderId, ownerId, amount);

        return "PostMyProperty";
    }
}
