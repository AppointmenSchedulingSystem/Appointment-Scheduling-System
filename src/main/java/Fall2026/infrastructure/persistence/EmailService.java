package Fall2026.infrastructure.persistence;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String fromEmail = "m2yazansalem@gmail.com";
    private final String appPassword = "";

    public void sendAppointmentConfirmation(String to, String username, String date, String time) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session mailSession = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, appPassword);
                    }
                });

        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Appointment Confirmation ✅");

            // 🔥 HTML EMAIL
            String htmlContent =
                    "<html>" +
                            "<body style='font-family: Arial; background-color:#f4f4f4; padding:20px;'>" +

                            "<div style='max-width:600px; margin:auto; background:white; padding:20px; border-radius:10px;'>" +

                            "<h2 style='color:#2e7d32;'>Appointment Confirmed ✔</h2>" +

                            "<p>Hello <b>" + username + "</b>,</p>" +

                            "<p>Your appointment has been successfully booked.</p>" +

                            "<table style='width:100%; margin-top:15px;'>" +
                            "<tr><td><b>Date:</b></td><td>" + date + "</td></tr>" +
                            "<tr><td><b>Time:</b></td><td>" + time + "</td></tr>" +
                            "</table>" +

                            "<p style='margin-top:20px;'>Thank you for using our system.</p>" +

                            "<hr>" +
                            "<p style='font-size:12px; color:gray;'>This is an automated message.</p>" +

                            "</div>" +
                            "</body>" +
                            "</html>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("📩 HTML Email sent to " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void sendGuestBookingEmail(String to, String username, String date, String time) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session mailSession = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, appPassword);
                    }
                });

        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            message.setSubject("Your Appointment is Confirmed 🎉");

            // 🔥 GUEST FRIENDLY HTML
            String htmlContent =
                    "<html>" +
                            "<body style='font-family: Arial; background-color:#f9f9f9; padding:20px;'>" +

                            "<div style='max-width:600px; margin:auto; background:white; padding:25px; border-radius:12px;'>" +

                            "<h2 style='color:#4CAF50;'>🎉 Booking Confirmed!</h2>" +

                            "<p>Hi <b>" + username + "</b>,</p>" +

                            "<p>Your appointment has been successfully booked. Here are the details:</p>" +

                            "<div style='background:#f1f1f1; padding:15px; border-radius:8px;'>" +
                            "<p><b>📅 Date:</b> " + date + "</p>" +
                            "<p><b>⏰ Time:</b> " + time + "</p>" +
                            "</div>" +

                            "<p style='margin-top:20px;'>We look forward to seeing you!</p>" +

                            "<hr>" +
                            "<p style='font-size:12px; color:gray;'>If you didn’t book this, please ignore this email.</p>" +

                            "</div>" +
                            "</body>" +
                            "</html>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("📩 Guest email sent to " + to);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}