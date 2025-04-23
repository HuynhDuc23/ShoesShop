package com.huynhduc.application.service;

import javax.mail.MessagingException;

public interface MailService {
    public void sendMailSimple(String to, String subject, String text ) throws MessagingException;
}
