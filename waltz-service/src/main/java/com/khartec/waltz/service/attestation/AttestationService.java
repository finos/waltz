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
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.attestation.Attestation;
import com.khartec.waltz.model.attestation.AttestationType;
import com.khartec.waltz.model.attestation.ImmutableAttestation;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.model.EntityKind.LOGICAL_DATA_FLOW;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_FLOW;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_SPECIFICATION;
import static com.khartec.waltz.model.attestation.AttestationType.IMPLICIT;


@Service
public class AttestationService {

    private final AttestationDao attestationDao;
    private final ChangeLogService changeLogService;
    private final LogicalFlowDao logicalFlowDao;
    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;


    @Autowired
    public AttestationService(AttestationDao attestationDao,
                              ChangeLogService changeLogService,
                              LogicalFlowDao logicalFlowDao,
                              PhysicalFlowDao physicalFlowDao,
                              PhysicalSpecificationDao physicalSpecificationDao) {
        checkNotNull(attestationDao, "attestationDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");

        this.attestationDao = attestationDao;
        this.changeLogService = changeLogService;
        this.logicalFlowDao = logicalFlowDao;
        this.physicalFlowDao = physicalFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
    }


    public List<Attestation> findForEntity(EntityReference entityReference) {
        return attestationDao.findForEntity(entityReference);
    }


    public boolean attestPhysicalFlow(AttestationType type, long id, String username, String comments) {

        boolean physicalAttestation = attest(PHYSICAL_FLOW, id, type, username, comments);

        // now attest the spec and logical flow implicitly
        PhysicalFlow physicalFlow = physicalFlowDao.getById(id);

        PhysicalSpecification spec = physicalSpecificationDao.getById(physicalFlow.specificationId());
        boolean specAttestation = attest(PHYSICAL_SPECIFICATION, spec.id().get(), IMPLICIT, username, comments + "  Due to attesation of physical flow: " + physicalFlow);

        LogicalFlow logical = logicalFlowDao.findBySourceAndTarget(spec.owningEntity(), physicalFlow.target());
        boolean logicalAttestation = attest(LOGICAL_DATA_FLOW, logical.id().get(), IMPLICIT, username, comments + "  Due to attesation of physical flow: " + physicalFlow);

        return physicalAttestation && specAttestation && logicalAttestation;
    }


    public boolean implicitlyAttest(EntityKind kind, long id, String username, String comments) {
        return attest(kind, id, IMPLICIT, username, comments);
    }


    public boolean explicitlyAttest(EntityKind kind, long id, String username, String comments) {
        return attest(kind, id, AttestationType.EXPLICIT, username, comments);
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


    private boolean attest(EntityKind kind, long id, AttestationType type, String username, String comments) {
        return create(ImmutableAttestation.builder()
                .entityReference(EntityReference.mkRef(kind, id))
                .attestationType(type)
                .attestedBy(username)
                .comments(comments)
                .build(), username);
    }


    private boolean create(Attestation attestation, String username) {

        ImmutableAttestation at = ImmutableAttestation
                .copyOf(attestation)
                .withAttestedAt(nowUtc())
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
