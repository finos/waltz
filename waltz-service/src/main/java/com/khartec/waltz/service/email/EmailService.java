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

import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.attestation.AttestationInstanceRecipientDao;
import com.khartec.waltz.data.attestation.AttestationRunDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.attestation.AttestationRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StreamUtilities.batchProcessingCollector;
import static java.util.stream.Collectors.toList;


@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
    private static final String MAIL_NEW_LINE = "<br/>";

    private final WaltzEmailer waltzEmailer;
    private final AttestationRunDao attestationRunDao;
    private final AttestationInstanceDao attestationInstanceDao;
    private final AttestationInstanceRecipientDao attestationInstanceRecipientDao;

    @Value("${waltz.email.batchSize:50}")
    private int batchSize;

    @Value("${waltz.base.url:localhost}")
    private String baseUrl;


    @Autowired
    public EmailService(WaltzEmailer waltzEmailer,
                        AttestationRunDao attestationRunDao,
                        AttestationInstanceDao attestationInstanceDao,
                        AttestationInstanceRecipientDao attestationInstanceRecipientDao) {
        checkNotNull(waltzEmailer, "waltzEmailer cannot be null");
        checkNotNull(attestationRunDao, "attestationRunDao cannot be null");
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(attestationInstanceRecipientDao, "attestationInstanceRecipientDao cannot be null");

        this.waltzEmailer = waltzEmailer;
        this.attestationRunDao = attestationRunDao;
        this.attestationInstanceDao = attestationInstanceDao;
        this.attestationInstanceRecipientDao = attestationInstanceRecipientDao;
    }


    public void sendEmailNotification(EntityReference ref) {
        switch (ref.kind()) {
            case ATTESTATION_RUN:
                sendAttestationRunNotification(ref);
                break;
            default:
                throw new UnsupportedOperationException("Cannot send default notification for ref: " + ref);
        }
    }


    // -- HELPERS ---

    private void sendAttestationRunNotification(EntityReference ref) {

        DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

        AttestationRun run = attestationRunDao.getById(ref.id());
        List<String> recipients = attestationInstanceRecipientDao.findRecipientsByRunId(run.id().get());
        List<AttestationInstance> instances = attestationInstanceDao.findByRunId(ref.id());

        List<String> recipientEmails = recipients.stream()
                .distinct()
                .collect(toList());

        List<String> applications = instances
                .stream()
                .map(d -> d.parentEntity())
                .filter(r -> r.kind().equals(EntityKind.APPLICATION))
                .map(r -> r.name())
                .filter(n -> n.isPresent())
                .map(n -> n.get())
                .distinct()
                .collect(toList());

        String subject = "Waltz attestation: " + run.name();

        String attestationsUrl = baseUrl + "/attestation/instance/user";
        String body = "You are required to attest correctness of the applications listed below:"
                + MAIL_NEW_LINE
                + MAIL_NEW_LINE
                + "<strong>Application(s):</strong> " + String.join(", ", applications)
                + MAIL_NEW_LINE
                + MAIL_NEW_LINE
                + "<strong>Due Date:</strong> " + run.dueDate().format(DATE_TIME_FORMATTER)
                + MAIL_NEW_LINE
                + MAIL_NEW_LINE
                + run.description()
                + MAIL_NEW_LINE
                + MAIL_NEW_LINE
                + "Please use this URL to view your pending attestations: " + attestationsUrl;

        sendEmailNotification(subject, body, recipientEmails);
    }


    private void sendEmailNotification(String subject, String body, Collection<String> recipients) {
        int count = recipients.stream().collect(batchProcessingCollector(batchSize, batch -> {
            String[] to = batch.toArray(new String[0]);
            waltzEmailer.sendEmail(subject, body, to);
        }));
        LOG.info(String.format("Sent email notification: %s to %s users", subject, count));
    }

}
