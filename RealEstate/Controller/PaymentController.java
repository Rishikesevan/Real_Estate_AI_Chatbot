package com.project.RealEstate.Controller;

import com.project.RealEstate.Service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class PaymentController {


    @Autowired
    private PaymentService paymentService;

    @GetMapping("/api/subscription")
    public String page(){
        return "Subscription";
    }


    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @PostMapping("/api/pay/{amount}")
    public String createOrder(
            @PathVariable("amount") int amount,
            Model model) throws RazorpayException {

        // Create Razorpay Order using user amount
        Order order = paymentService.createOrder(amount);

        model.addAttribute("orderId", order.get("id"));
        model.addAttribute("amount", amount);
        model.addAttribute("key", razorpayKey);

        return "Razorpay";
    }
}
