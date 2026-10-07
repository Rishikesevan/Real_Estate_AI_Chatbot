package com.project.RealEstate.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {

        String link = "http://localhost:8080/verify?token=" + token;
        System.out.println("------------Link:------------ " + link);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify Your Account - RealEstate");

            String htmlContent = "<html>" +
                    "<body style='font-family: Arial, sans-serif;'>" +

                    "<h2 style='color:#2c3e50;'>Welcome to RealEstate 🏠</h2>" +

                    "<p>Thank you for registering with us.</p>" +

                    "<p>Please click the button below to verify your email address and activate your account:</p>" +

                    "<p style='margin:20px 0;'>" +
                    "<a href=\"" + link + "\" " +
                    "style='background-color:#28a745;color:white;padding:10px 20px;" +
                    "text-decoration:none;border-radius:5px;display:inline-block;'>"
                    + "Verify Account</a>" +
                    "</p>" +

                    "<p>If the button above does not work, copy and paste the link below into your browser:</p>" +

                    "<p style='color:blue;'>" + link + "</p>" +

                    "<br>" +
                    "<p>Thanks & Regards,<br>RealEstate Team</p>" +

                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("Mail Sent To: " + toEmail);

        } catch (Exception e) {
            System.out.println("mail send failed, use this link manually: " + link);
            System.out.println("Exception: " + e.getMessage());
            // e.printStackTrace();
        }
    }

}
