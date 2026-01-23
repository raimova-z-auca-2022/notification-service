package com.example.service;

import com.example.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(Notification n) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(n.getRecipient());
        msg.setSubject(n.getSubject());
        msg.setText(n.getMessageBody());
        mailSender.send(msg);
    }
}
