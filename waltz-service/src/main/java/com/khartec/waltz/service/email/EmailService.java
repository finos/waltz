package com.khartec.waltz.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StreamUtilities.batchProcessingCollector;


@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    private WaltzEmailer waltzEmailer;

    @Value("${waltz.email.batchSize:50}")
    private int batchSize;


    @Autowired
    public EmailService(WaltzEmailer waltzEmailer) {
        checkNotNull(waltzEmailer, "waltzEmailer cannot be null");

        this.waltzEmailer = waltzEmailer;
    }


    public void sendEmailNotification(String subject, String body, Collection<String> recipients) {
        int count = recipients.stream().collect(batchProcessingCollector(batchSize, batch -> {
            String[] to = batch.toArray(new String[batch.size()]);
            waltzEmailer.sendEmail(subject, body, to);
        }));
        LOG.info(String.format("Sent email notification: %s to %s users", subject, count));
    }

}
