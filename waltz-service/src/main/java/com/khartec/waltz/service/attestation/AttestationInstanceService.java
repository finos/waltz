package com.khartec.waltz.service.attestation;

import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;


@Service
public class AttestationInstanceService {

    private final AttestationInstanceDao attestationInstanceDao;
    private final PersonDao personDao;
    private final ChangeLogService changeLogService;


    public AttestationInstanceService(AttestationInstanceDao attestationInstanceDao,
                                      PersonDao personDao, ChangeLogService changeLogService) {
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.personDao = personDao;
        this.changeLogService = changeLogService;
    }


    public List<AttestationInstance> findByRecipient(String userId, boolean unattestedOnly) {
        checkNotNull(userId, "userId cannot be null");

        return attestationInstanceDao.findByRecipient(userId, unattestedOnly);
    }


    public List<AttestationInstance> findHistoricalForPendingByUserId(String userId) {
        checkNotNull(userId, "userId cannot be null");

        return attestationInstanceDao.findHistoricalForPendingByUserId(userId);
    }


    public List<AttestationInstance> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return attestationInstanceDao.findByEntityReference(ref);
    }


    public boolean attestInstance(long instanceId, String attestedBy) {
        checkNotEmpty(attestedBy, "attestedBy must be provided");

        boolean success = attestationInstanceDao.attestInstance(instanceId, attestedBy, nowUtc());
        if(success) {
            AttestationInstance instance = attestationInstanceDao.getById(instanceId);
            logChange(attestedBy, instance);
        }
        return success;
    }


    public List<AttestationInstance> findByRunId(long runId) {
        return attestationInstanceDao.findByRunId(runId);
    }


    public List<Person> findPersonsByInstanceId(long id) {
        return personDao.findPersonsByAttestationInstanceId(id);
    }


    public int cleanupOrphans() {
        return attestationInstanceDao.cleanupOrphans();
    }


    private void logChange (String username, AttestationInstance instance) {

        changeLogService.write(ImmutableChangeLog.builder()
                .message(String.format("Attestation of %s", instance.childEntityKind()))
                .parentReference(instance.parentEntity())
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(instance.childEntityKind())
                .operation(Operation.ADD)
                .build());
    }

}
