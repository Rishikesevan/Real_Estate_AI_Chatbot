package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.Agent;
import com.project.RealEstate.Entity.AgentSubscription;
import com.project.RealEstate.Repository.AgentRepository;
import com.project.RealEstate.Repository.AgentSubscriptionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AgentSubscriptionController {

    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private AgentSubscriptionRepository subscriptionRepository;

    public AgentSubscriptionController(AgentRepository agentRepository,
                                       AgentSubscriptionRepository subscriptionRepository) {
        this.agentRepository = agentRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // =====================================================
    // CHECK ACTIVE SUBSCRIPTION (Page Load)
    // =====================================================
    @GetMapping("/agent/subscription/status")
    @ResponseBody
    public boolean hasActiveSubscription(HttpSession session) {

        Long agentId = (Long) session.getAttribute("agentId");
        if (agentId == null) {
            return false;
        }

        return subscriptionRepository
                .findTopByAgentIdOrderByEndDateDesc(agentId)
                .map(sub ->
                        sub.isActive() &&
                                sub.getEndDate().isAfter(LocalDateTime.now())
                )
                .orElse(false);
    }


    @GetMapping("/agent/createOrder/{plan}")
    @ResponseBody
    public Map<String, Object> createOrder(
            @PathVariable String plan,
            HttpSession session) throws Exception {

        Long agentId = (Long) session.getAttribute("agentId");
        System.out.println("AGENT ID = " + agentId);

        int amount = "ONE_WEEK".equals(plan) ? 50000 : 150000;

        RazorpayClient client =
                new RazorpayClient(razorpayKey, razorpaySecret);

        JSONObject options = new JSONObject();
        options.put("amount", amount);
        options.put("currency", "INR");
        options.put("receipt", "order_" + System.currentTimeMillis());

        Order order = client.orders.create(options);

        // ✅ Convert to Map for Spring
        Map<String, Object> response = new HashMap<>();
        response.put("id", order.get("id"));
        response.put("amount", order.get("amount"));
        response.put("currency", order.get("currency"));

        return response;
    }



    // =====================================================
    // PAYMENT SUCCESS → SAVE TO DATABASE
    // URL: /agent/payment/success
    // =====================================================
    @PostMapping("/agent/payment/success")
    @ResponseBody
    public String paymentSuccess(
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpaySignature,
            @RequestParam String plan,
            HttpSession session) {

        Long agentId = (Long) session.getAttribute("agentId");
        if (agentId == null) return "NOT_LOGGED_IN";

        Agent agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null) return "AGENT_NOT_FOUND";

        try {
            JSONObject payload = new JSONObject();
            payload.put("razorpay_payment_id", razorpayPaymentId);
            payload.put("razorpay_order_id", razorpayOrderId);
            payload.put("razorpay_signature", razorpaySignature);

            boolean valid = com.razorpay.Utils.verifyPaymentSignature(
                    payload, razorpaySecret);

            if (!valid) return "SIGNATURE_INVALID";

            AgentSubscription sub = new AgentSubscription();
            sub.setAgent(agent);
            sub.setPlan(plan);
            sub.setRazorpayPaymentId(razorpayPaymentId);
            sub.setRazorpayOrderId(razorpayOrderId);
            sub.setStartDate(LocalDateTime.now());
            sub.setEndDate(
                    "ONE_WEEK".equals(plan)
                            ? LocalDateTime.now().plusWeeks(1)
                            : LocalDateTime.now().plusMonths(1)
            );
            sub.setActive(true);

            subscriptionRepository.save(sub);
            return "success";

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }


}
