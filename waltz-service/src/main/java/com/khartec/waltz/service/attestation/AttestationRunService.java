package com.khartec.waltz.service.attestation;


import com.khartec.waltz.data.attestation.AttestationRunDao;
import com.khartec.waltz.model.IdCommandResponse;
import com.khartec.waltz.model.ImmutableIdCommandResponse;
import com.khartec.waltz.model.attestation.AttestationRun;
import com.khartec.waltz.model.attestation.AttestationRunCreateCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class AttestationRunService {

    private final AttestationRunDao attestationRunDao;


    @Autowired
    public AttestationRunService(AttestationRunDao attestationRunDao) {
        checkNotNull(attestationRunDao, "attestationRunDao cannot be null");

        this.attestationRunDao = attestationRunDao;
    }


    public AttestationRun getById(long attestationRunId) {
        return attestationRunDao.getById(attestationRunId);
    }


    public IdCommandResponse create(String userId, AttestationRunCreateCommand command) {
        // create run
        Long runId = attestationRunDao.create(userId, command);

        // create instance

        // create instance recipients

        return ImmutableIdCommandResponse.builder()
                .id(runId)
                .build();
    }

}
