package com.example.librarymailservice.listener;

import com.example.librarymailservice.model.MailInfoRabbit;
import com.example.librarymailservice.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqListener {
    private final MailService mailService;

    @RabbitListener(queues = "library-queue")
    public void sendMail(MailInfoRabbit mailInfoRabbit) {
        mailService.sendMessages(mailInfoRabbit);
    }
}
