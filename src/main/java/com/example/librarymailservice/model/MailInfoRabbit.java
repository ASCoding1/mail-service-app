package com.example.librarymailservice.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MailInfoRabbit {
    private List<String> subscriberEmails = new ArrayList<>();
    private List<BookInfo> bookInfoList = new ArrayList<>();
}
