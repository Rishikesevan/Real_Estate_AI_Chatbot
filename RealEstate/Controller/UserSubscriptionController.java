package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Entity.UserSubscription;
import com.project.RealEstate.Repository.UserRepository;
import com.project.RealEstate.Repository.UserSubscriptionRepository;
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
public class UserSubscriptionController {

    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubscriptionRepository subscriptionRepository;

    public UserSubscriptionController(UserRepository userRepository,
                                      UserSubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // =====================================================
    // CHECK ACTIVE SUBSCRIPTION (Page Load)
    // =====================================================
    @GetMapping("/api/user/subscription/status")
    @ResponseBody
    public boolean hasActiveSubscription(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return false;
        }

        return subscriptionRepository
                .findTopByUserIdOrderByEndDateDesc(userId)
                .map(sub ->
                        sub.isActive() &&
                                sub.getEndDate().isAfter(LocalDateTime.now())
                )
                .orElse(false);
    }

    // =====================================================
    // CREATE RAZORPAY ORDER
    // =====================================================
    @GetMapping("/api/user/createOrder/{plan}")
    @ResponseBody
    public Map<String, Object> createOrder(
            @PathVariable String plan,
            HttpSession session) throws Exception {

        Long userId = (Long) session.getAttribute("userId");
        System.out.println("USER ID = " + userId);

        int amount = "ONE_WEEK".equals(plan) ? 10000 : 35000; // amount in paise

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);

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
    // URL: /user/payment/success
    // =====================================================
    @PostMapping("/api/user/payment/success")
    @ResponseBody
    public String paymentSuccess(
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpaySignature,
            @RequestParam String plan,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "NOT_LOGGED_IN";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return "USER_NOT_FOUND";

        try {
            // Verify signature
            JSONObject payload = new JSONObject();
            payload.put("razorpay_payment_id", razorpayPaymentId);
            payload.put("razorpay_order_id", razorpayOrderId);
            payload.put("razorpay_signature", razorpaySignature);

            boolean valid = com.razorpay.Utils.verifyPaymentSignature(payload, razorpaySecret);
            if (!valid) return "SIGNATURE_INVALID";

//            LocalDateTime startDate = LocalDateTime.now();
//            LocalDateTime endDate = startDate.plusMinutes(1);

            // Save subscription
            UserSubscription subscription = new UserSubscription();
            subscription.setUser(user);
            subscription.setPlan(plan);
            subscription.setRazorpayPaymentId(razorpayPaymentId);
            subscription.setRazorpayOrderId(razorpayOrderId);
//            subscription.setStartDate(startDate);
//            subscription.setEndDate(endDate);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(
                    "ONE_WEEK".equals(plan)
                            ? LocalDateTime.now().plusWeeks(1)
                            : LocalDateTime.now().plusMonths(1)
            );
            subscription.setActive(true);

            subscriptionRepository.save(subscription);
            return "success";

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
}
