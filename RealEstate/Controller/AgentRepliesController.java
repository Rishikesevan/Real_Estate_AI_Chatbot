package com.project.RealEstate.Controller;

import com.project.RealEstate.Entity.AgentReplies;
import com.project.RealEstate.Entity.User;
import com.project.RealEstate.Service.AgentRepliesService;
import com.project.RealEstate.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AgentRepliesController {
    @Autowired
    private UserService userService;

    @Autowired
    private AgentRepliesService agentRepliesService;

    @GetMapping("/api/reply/agentresponse")
    public String getAgentReply(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/api/login?errors";
        }

        User user = userService.getUserById(userId);

        // 1. Fetch the flat list of replies from your service
        List<AgentReplies> agentReplies = agentRepliesService.getAgentRepliesByUserId(userId);

        // 2. Group the list into a Map where the Key is the Agent and the Value is
        // their list of replies
        // This assumes AgentReplies has a getEnquiry() method which has a getAgent()
        // method
        Map<Object, List<AgentReplies>> groupedReplies = agentReplies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getEnquiry().getAgent()));

        // 3. Add the grouped map to the model
        model.addAttribute("agentConversations", groupedReplies);

        // Optional: Keep the original 'replies' attribute if you have other logic
        // depending on it
        model.addAttribute("replies", agentReplies);
        model.addAttribute("user", user);

        return "AgentResponse";
    }
    // @GetMapping("/api/agent/logout")
    // public String logout(HttpSession session)
    // {
    // session.invalidate(); // 💣 clears userId
    // return "redirect:/api/homeIndex";
    // }
}
