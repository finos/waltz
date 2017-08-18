package com.khartec.waltz.service.attestation;

import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.model.attestation.AttestationInstance;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotEmpty;
import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;


@Service
public class AttestationInstanceService {

    private final AttestationInstanceDao attestationInstanceDao;


    public AttestationInstanceService(AttestationInstanceDao attestationInstanceDao) {
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        this.attestationInstanceDao = attestationInstanceDao;
    }


    public List<AttestationInstance> findByRecipient(String userId) {
        checkNotNull(userId, "userId cannot be null");

        return attestationInstanceDao.findByRecipient(userId);
    }


    public boolean attestInstance(long instanceId, String attestedBy) {
        checkNotEmpty(attestedBy, "attestedBy must be provided");

        return attestationInstanceDao.attestInstance(instanceId, attestedBy, nowUtc());
    }


    public List<AttestationInstance> findByRunId(long runId) {
        return attestationInstanceDao.findByRunId(runId);
    }

}
