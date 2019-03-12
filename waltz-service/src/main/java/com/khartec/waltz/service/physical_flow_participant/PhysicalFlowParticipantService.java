/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

package com.khartec.waltz.service.physical_flow_participant;

import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.physical_flow_participant.PhysicalFlowParticipantDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.physical_flow_participant.ParticipationKind;
import com.khartec.waltz.model.physical_flow_participant.PhysicalFlowParticipant;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_FLOW;
import static java.lang.String.format;


@Service
public class PhysicalFlowParticipantService {

    private final PhysicalFlowParticipantDao dao;
    private final ChangeLogService changeLogService;
    private final EntityReferenceNameResolver nameResolver;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalFlowService physicalFlowService;
    private final PhysicalSpecificationService physicalSpecificationService;

    @Autowired
    public PhysicalFlowParticipantService(PhysicalFlowParticipantDao dao,
                                          ChangeLogService changeLogService,
                                          EntityReferenceNameResolver nameResolver,
                                          LogicalFlowService logicalFlowService,
                                          PhysicalFlowService physicalFlowService, 
                                          PhysicalSpecificationService physicalSpecificationService) {
        checkNotNull(dao, "dao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");
        
        this.dao = dao;
        this.changeLogService = changeLogService;
        this.nameResolver = nameResolver;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowService = physicalFlowService;
        this.physicalSpecificationService = physicalSpecificationService;
    }


    public Collection<PhysicalFlowParticipant> findByPhysicalFlowId(long id) {
        return dao.findByPhysicalFlowId(id);
    }

    public Collection<PhysicalFlowParticipant> findByParticipant(EntityReference entityReference) {
        checkNotNull(entityReference, "entityReference cannot be null");
        return dao.findByParticipant(entityReference);
    }

    public Boolean remove(long physicalFlowId,
                          ParticipationKind participationKind,
                          EntityReference participant,
                          String username) {

        checkNotNull(participationKind, "participationKind cannot be null");

        boolean result = dao.remove(physicalFlowId, participationKind, participant);
        if (result) {
            writeToAuditLog("Removed", physicalFlowId, participant, username);
        }
        return result;
    }


    public Boolean add(long physicalFlowId,
                          ParticipationKind participationKind,
                          EntityReference participant,
                          String username) {

        checkNotNull(participationKind, "participationKind cannot be null");

        boolean result = dao.add(physicalFlowId, participationKind, participant, username);
        if (result) {
            writeToAuditLog("Added", physicalFlowId, participant, username);
        }
        return result;
    }


    // --- helpers ---

    private void writeToAuditLog(String verb,
                                 long physicalFlowId,
                                 EntityReference participant,
                                 String username) {
        nameResolver
                .resolve(participant)
                .ifPresent(p -> {
                    String msg = format("%s participant: [%s] to physical flow", verb, p.name().orElse("?"));
                    auditChange(msg, EntityReference.mkRef(PHYSICAL_FLOW, physicalFlowId), username, Operation.ADD);
                });

        PhysicalFlow physicalFlow = physicalFlowService.getById(physicalFlowId);
        if (physicalFlow != null) {
            LogicalFlow logicalFlow = logicalFlowService.getById(physicalFlow.logicalFlowId());
            PhysicalSpecification specification = physicalSpecificationService.getById(physicalFlow.specificationId());
            if (logicalFlow != null && specification != null) {
                auditChange(
                        format(
                                "%s as a participant to flow: (%s) -[%s]-> (%s)",
                                verb,
                                logicalFlow.source().name().orElse("?"),
                                specification.name(),
                                logicalFlow.target().name().orElse("?")),
                        participant,
                        username,
                        Operation.ADD);
            }
        }
    }


    private void auditChange(String message, EntityReference ref, String username, Operation operation) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .severity(Severity.INFORMATION)
                .userId(username)
                .message(message)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }

}
