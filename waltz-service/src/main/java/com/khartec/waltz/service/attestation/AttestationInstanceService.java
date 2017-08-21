package com.khartec.waltz.service.attestation;

import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.person.Person;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;


@Service
public class AttestationInstanceService {

    private final AttestationInstanceDao attestationInstanceDao;
    private final PersonDao personDao;


    public AttestationInstanceService(AttestationInstanceDao attestationInstanceDao,
                                      PersonDao personDao) {
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(personDao, "personDao cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.personDao = personDao;
    }


    public List<AttestationInstance> findByRecipient(String userId) {
        checkNotNull(userId, "userId cannot be null");

        return attestationInstanceDao.findByRecipient(userId);
    }


    public List<AttestationInstance> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");

        return attestationInstanceDao.findByEntityReference(ref);
    }


    public boolean attestInstance(long instanceId, String attestedBy) {
        checkNotEmpty(attestedBy, "attestedBy must be provided");

        return attestationInstanceDao.attestInstance(instanceId, attestedBy, nowUtc());
    }


    public List<AttestationInstance> findByRunId(long runId) {
        return attestationInstanceDao.findByRunId(runId);
    }


    public List<Person> findPersonsByInstanceId(long id) {
        return personDao.findPersonsByAttestationInstanceId(id);
    }

}
