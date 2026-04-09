package org.example;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class Email implements NotificationObserver {

    public Email() {

    }

    public void sendEmail(String email, String messageText) {

        if (email == null) {
            System.out.println("Invalid email format, skipping...");
            return;
        }

        final String from = "momenx22005@gmail.com";
        final String password = "hsegenctvtbmqekz";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            msg.setSubject("AppointEase");
            msg.setText("Hope this email finds you well, This is from your AppointEase Application\n\n\n" + messageText);

            Transport.send(msg);

            System.out.println("Email sent to: " + email);

        } catch (SendFailedException e) {
            System.out.println("Email failed (invalid or non-existing): " + email);

        } catch (MessagingException e) {
            System.out.println("Error sending email to: " + email);
            e.printStackTrace();
        }
    }

    @Override
    public void update(String message, User user, Administrator admin, NotificationType type) {
        sendEmail(user.getEmail(), message);
    }
}