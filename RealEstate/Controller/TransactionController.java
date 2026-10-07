package com.project.RealEstate.Controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.RealEstate.Entity.Transaction;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Service.TransactionService;
import com.project.RealEstate.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;

    @GetMapping("/api/view/transaction")
    public String viewTransaction(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);

        List<Transaction> transactions = transactionService.viewAllTransaction();

        int allTransaction = transactions.size();
        int soldDate = 0;

        long totalPrice = 0L; 
        long totalSoldThisMonth = 0L;

        LocalDate today = LocalDate.now();

        for (Transaction transaction : transactions) {

            // SAFETY CHECK
            if (transaction.getSoldPrice() != null) {
                totalPrice += Math.round(transaction.getSoldPrice());
            }

            if (transaction.getSoldDate() != null &&
                    transaction.getSoldDate().getMonth() == today.getMonth() &&
                    transaction.getSoldDate().getYear() == today.getYear()) {

                soldDate++;
                totalSoldThisMonth += Math.round(transaction.getSoldPrice());
            }
        }

        long average = 0L;
        if (allTransaction > 0) {
            average = totalPrice / allTransaction;
        }

        model.addAttribute("viewall", allTransaction);
        model.addAttribute("soldDate", soldDate);
        model.addAttribute("totalPrice", totalSoldThisMonth);
        model.addAttribute("average", average);
        model.addAttribute("viewAllTransaction", transactions);

        return "Transaction";
    }

}
