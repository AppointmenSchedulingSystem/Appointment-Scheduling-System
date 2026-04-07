package Fall2026.infrastructure.notification;

import Fall2026.domain.account.Role;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Real notification service — sends HTML emails via Gmail SMTP.
 * Credentials are loaded from src/main/resources/config.properties
 */
public class NotificationService implements Observer {

    private final String fromEmail;
    private final String appPassword;
    private final Session mailSession;

    public NotificationService() {
        Properties config = loadConfig();
        this.fromEmail   = config.getProperty("mail.username");
        this.appPassword = config.getProperty("mail.password");
        this.mailSession = buildSession();
    }

    @Override
    public void notify(Role role, String message) {
        try {
            Message email = new MimeMessage(mailSession);
            email.setFrom(new InternetAddress(fromEmail, "Appointment System"));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(role.getEmail()));
            email.setSubject("📅 Appointment Notification");

            String html = buildHtml(role.getUsername(), message);

            email.setContent(html, "text/html; charset=utf-8");
            Transport.send(email);

            System.out.println("[NOTIFICATION] Email sent to " + role.getEmail());
        } catch (Exception e) {
            System.err.println("[NOTIFICATION] Failed to send email to "
                    + role.getEmail() + ": " + e.getMessage());
        }
    }

    // --- HTML builder ---
    // AI generated HTML template for email notifications, with inline CSS for better compatibility across email clients.
    private String buildHtml(String username, String message) {
        return "<!DOCTYPE html>" +
                "<html lang='en'><head><meta charset='UTF-8'>" +
                "<style>" +
                "  body { margin: 0; padding: 0; background: #f4f6f8; font-family: Arial, sans-serif; }" +
                "  .wrapper { max-width: 520px; margin: 40px auto; background: #ffffff;" +
                "             border-radius: 10px; overflow: hidden;" +
                "             box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                "  .header { background: #4f46e5; padding: 30px; text-align: center; }" +
                "  .header h1 { color: #ffffff; margin: 0; font-size: 22px; letter-spacing: 1px; }" +
                "  .header p  { color: #c7d2fe; margin: 6px 0 0; font-size: 13px; }" +
                "  .body { padding: 32px 36px; }" +
                "  .greeting { font-size: 16px; color: #374151; margin-bottom: 16px; }" +
                "  .message-box { background: #f0f4ff; border-left: 4px solid #4f46e5;" +
                "                 border-radius: 6px; padding: 16px 20px;" +
                "                 color: #1e293b; font-size: 15px; line-height: 1.6; }" +
                "  .footer { background: #f9fafb; border-top: 1px solid #e5e7eb;" +
                "            padding: 18px 36px; text-align: center;" +
                "            font-size: 12px; color: #9ca3af; }" +
                "</style></head><body>" +
                "<div class='wrapper'>" +
                "  <div class='header'>" +
                "    <h1>📅 Appointment System</h1>" +
                "    <p>Notification from your scheduling service</p>" +
                "  </div>" +
                "  <div class='body'>" +
                "    <p class='greeting'>Hello, <strong>" + username + "</strong> 👋</p>" +
                "    <div class='message-box'>" + message + "</div>" +
                "  </div>" +
                "  <div class='footer'>" +
                "    This is an automated message — please do not reply.<br>" +
                "    &copy; 2026 Appointment Scheduling System" +
                "  </div>" +
                "</div>" +
                "</body></html>";
    }

    // --- Private helpers ---

    private Session buildSession() {
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth",            "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host",            "smtp.gmail.com");
        smtpProps.put("mail.smtp.port",            "587");

        return Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }

    private Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException(
                        "config.properties not found in resources. " +
                                "Make sure it exists at src/main/resources/config.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties: " + e.getMessage());
        }
        return props;
    }
}