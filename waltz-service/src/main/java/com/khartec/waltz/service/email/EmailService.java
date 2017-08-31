package com.khartec.waltz.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class EmailService {

    private WaltzEmailer waltzEmailer;

    @Autowired
    public EmailService(WaltzEmailer waltzEmailer) {
        checkNotNull(waltzEmailer, "waltzEmailer cannot be null");

        this.waltzEmailer = waltzEmailer;
    }


    public void sendEmailNotification(String subject, String body, Collection<String> recipients) {
        String[] to = recipients.toArray(new String[recipients.size()]);
        waltzEmailer.sendEmail(subject, body, to);
    }

}
