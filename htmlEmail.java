import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Method to send HTML email
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // Using MimeMessageHelper to create a MimeMessage
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);  // 'true' indicates that the body is HTML
        helper.setFrom("your-email@example.com");

        // Send the email
        mailSender.send(mimeMessage);
    }
}

String htmlBody = "<h1>Welcome to Spring Boot Email Service</h1>"
                + "<p>This is an email in <b>HTML format</b> sent using Spring Boot.</p>";

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/send-html-email")
    public String sendHtmlEmail() {
        String htmlBody = "<h1>Spring Boot HTML Email</h1>"
                + "<p>This is a <b>sample HTML</b> email.</p>";
        try {
            emailService.sendHtmlEmail("recipient@example.com", "Test HTML Email", htmlBody);
            return "HTML Email sent successfully!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error while sending email!";
        }
    }
}
