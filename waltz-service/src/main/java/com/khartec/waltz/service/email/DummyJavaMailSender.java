/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

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
