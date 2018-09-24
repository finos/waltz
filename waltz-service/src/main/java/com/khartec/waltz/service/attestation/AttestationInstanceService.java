/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

import com.khartec.waltz.data.attestation.AttestationInstanceDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.attestation.AttestationRun;
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
    private final AttestationRunService attestationRunService;
    private final PersonDao personDao;
    private final ChangeLogService changeLogService;


    public AttestationInstanceService(AttestationInstanceDao attestationInstanceDao,
                                      AttestationRunService attestationRunService,
                                      PersonDao personDao, ChangeLogService changeLogService) {
        checkNotNull(attestationInstanceDao, "attestationInstanceDao cannot be null");
        checkNotNull(attestationRunService, "attestationRunService cannot be null");
        checkNotNull(personDao, "personDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.attestationInstanceDao = attestationInstanceDao;
        this.attestationRunService = attestationRunService;
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
            AttestationRun run = attestationRunService.getById(instance.attestationRunId());
            logChange(attestedBy, instance, run.attestedEntityKind());
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


    private void logChange (String username, AttestationInstance instance, EntityKind attestedKind) {

        changeLogService.write(ImmutableChangeLog.builder()
                .message(String.format("Attestation of %s", attestedKind))
                .parentReference(instance.parentEntity())
                .userId(username)
                .severity(Severity.INFORMATION)
                .childKind(attestedKind)
                .operation(Operation.ADD)
                .build());
    }

}
