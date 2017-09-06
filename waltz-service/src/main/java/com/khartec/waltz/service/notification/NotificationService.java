package com.khartec.waltz.service.notification;


import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.DBExecutorPoolInterface;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.notification.ImmutableNotificationSummary;
import com.khartec.waltz.model.notification.NotificationSummary;
import com.khartec.waltz.service.attestation.AttestationInstanceService;
import com.khartec.waltz.service.survey.SurveyInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class NotificationService {

    private final AttestationInstanceService attestationInstanceService;
    private final SurveyInstanceService surveyInstanceService;
    private final DBExecutorPoolInterface dbExecutorPool;


    @Autowired
    public NotificationService(AttestationInstanceService attestationInstanceService,
                               DBExecutorPoolInterface dbExecutorPool,
                               SurveyInstanceService surveyInstanceService) {
        checkNotNull(attestationInstanceService, "attestationInstanceService cannot be null");
        checkNotNull(dbExecutorPool, "dbExecutorPool cannot be null");
        checkNotNull(surveyInstanceService, "surveyInstanceService cannot be null");

        this.attestationInstanceService = attestationInstanceService;
        this.dbExecutorPool = dbExecutorPool;
        this.surveyInstanceService = surveyInstanceService;
    }


    public List<NotificationSummary> findNotificationsByUserId(String userId) throws ExecutionException, InterruptedException {
        Future<Integer> pendingAttestationCountFuture = dbExecutorPool.submit(() -> attestationInstanceService.findPendingCountByRecipient(userId));
        Future<Integer> pendingSurveyCountFuture = dbExecutorPool.submit(() -> surveyInstanceService.findPendingCountForRecipient(userId));


        return ListUtilities.newArrayList(
                ImmutableNotificationSummary.builder()
                        .kind(EntityKind.ATTESTATION)
                        .count(pendingAttestationCountFuture.get())
                        .build(),
                ImmutableNotificationSummary.builder()
                        .kind(EntityKind.SURVEY_INSTANCE)
                        .count(pendingSurveyCountFuture.get())
                        .build()
        );
    }


}
