package com.project.RealEstate.Controller;

import com.project.RealEstate.Service.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReplyController {
    @Autowired
    private ReplyService replyService;

    @PostMapping("/api/save/agentreplies")
    @ResponseBody
    public String saveAgentReply(@RequestParam Long enquiryId, @RequestParam String message) {
        replyService.saveReplies(enquiryId, message);
        return "success";
    }

}
