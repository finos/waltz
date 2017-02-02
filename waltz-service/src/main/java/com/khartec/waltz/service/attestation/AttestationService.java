/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.attestation;


import com.khartec.waltz.data.attestation.AttestationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.attestation.Attestation;
import com.khartec.waltz.model.attestation.ImmutableAttestation;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Service
public class AttestationService {

    private final AttestationDao attestationDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public AttestationService(AttestationDao attestationDao, ChangeLogService changeLogService) {
        checkNotNull(attestationDao, "attestationDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.attestationDao = attestationDao;
        this.changeLogService = changeLogService;
    }


    public List<Attestation> findForEntity(EntityReference entityReference) {
        return attestationDao.findForEntity(entityReference);
    }


    public boolean create(Attestation attestation, String username) {

        ImmutableAttestation at = ImmutableAttestation
                .copyOf(attestation)
                .withAttestedBy(username);

        boolean success = attestationDao.create(at);
        if(success) {
            logChange(username,
                    attestation.entityReference(),
                    "Attestation created",
                    Operation.ADD);
        }
        return success;
    }


    public boolean deleteForEntity(EntityReference reference, String username) {
        boolean success = attestationDao.deleteForEntity(reference);
        if(success) {
            logChange(username,
                    reference,
                    "Attestation deleted due to parent deletion",
                    Operation.REMOVE);
        }
        return success;
    }


    private void logChange(String userId,
                           EntityReference ref,
                           String message,
                           Operation operation) {

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .childKind(EntityKind.ATTESTATION)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }

}
