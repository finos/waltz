package com.khartec.waltz.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;

public class DummyJavaMailSender implements JavaMailSender {

    private final Logger LOG = LoggerFactory.getLogger(DummyJavaMailSender.class);

    @Override
    public MimeMessage createMimeMessage() {
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream inputStream) throws MailException {
        return null;
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        nop();
    }


    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        nop();
    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        nop();
    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        nop();
    }

    @Override
    public void send(SimpleMailMessage simpleMailMessage) throws MailException {
        nop();
    }

    @Override
    public void send(SimpleMailMessage... simpleMailMessages) throws MailException {
        nop();
    }

    private void nop() {
        LOG.debug("This is a do-nothing implementation of the JavaMailSender interface, any attempts to send an email will not work");
    }

}
