package com.example.librarymailservice.service;

import com.example.librarymailservice.model.BookInfo;
import com.example.librarymailservice.model.MailInfoRabbit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor

public class MailService {
    private final JavaMailSender emailSender;
    private final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final int corePoolSize = 2;
    private final int maxPoolSize = corePoolSize * 2;
    private final long keepAliveTime = 60L;
    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue
    );

    public void sendMessages(MailInfoRabbit mailInfoRabbit) {
        List<String> subscriberEmails = mailInfoRabbit.getSubscriberEmails();

        for (String email : subscriberEmails) {
            executorService.execute(() -> {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("asobolewskitest97@gmail.com");
                message.setTo(email);
                message.setSubject("NEW BOOKS IN OUR LIBRARY IN YOUR CATEGORY:");
                message.setText(buildMailContent(mailInfoRabbit));

                int maxRetryAttempts = 3;
                int retryCount = 0;

                while (retryCount < maxRetryAttempts) {
                    try {
                        emailSender.send(message);
                        logger.info("Email sent successfully to: {}", email);
                        break;
                    } catch (MailSendException e) {
                        retryCount++;
                        logger.error("Error sending email to {}: Attempt {}/{} - {}", email, retryCount, maxRetryAttempts, e.getMessage(), e);

                        if (retryCount < maxRetryAttempts) {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        } else {
                            logger.error("Max retry attempts reached. Could not send email to: {}", email);
                        }
                    }
                }
            });
        }
    }

    private String buildMailContent(MailInfoRabbit mailInfoRabbit) {
        List<BookInfo> bookInfoList = mailInfoRabbit.getBookInfoList();
        String category = "";

        if (!bookInfoList.isEmpty()) {
            category = bookInfoList.get(0).getCategory();
        }

        StringBuilder mailContent = new StringBuilder("New books in category: ")
                .append(category)
                .append("\n with titles:\n");

        for (BookInfo bookInfo : bookInfoList) {
            mailContent.append("- ").append(bookInfo.getTitle()).append("\n");
        }

        return mailContent.toString();
    }
}