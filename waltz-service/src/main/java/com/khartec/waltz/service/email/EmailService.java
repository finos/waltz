package com.khartec.waltz.service.email;

import com.khartec.waltz.model.email.EmailNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EmailService {

    private WaltzEmailer waltzEmailer;

    @Autowired
    public EmailService(WaltzEmailer waltzEmailer) {
        checkNotNull(waltzEmailer, "waltzEmailer cannot be null");

        this.waltzEmailer = waltzEmailer;
    }


    public void sendEmailNotification(EmailNotification emailNotification) {

        String[] to = emailNotification.recipients().toArray(new String[emailNotification.recipients().size()]);
        String subject = emailNotification.subject();
        String body = emailNotification.body();

        waltzEmailer.sendEmail(subject, body, to);
    }

}
