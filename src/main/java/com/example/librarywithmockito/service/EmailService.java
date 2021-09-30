package com.example.librarywithmockito.service;

import java.util.List;

public interface EmailService {

    void sendMails(String message, List<String> mailsList);

}
