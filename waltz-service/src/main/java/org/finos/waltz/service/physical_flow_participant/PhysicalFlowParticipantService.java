/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package org.finos.waltz.service.physical_flow_participant;

import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.logical_flow.LogicalFlowService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.physical_flow_participant.PhysicalFlowParticipantDao;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.finos.waltz.model.physical_flow_participant.ParticipationKind;
import org.finos.waltz.model.physical_flow_participant.PhysicalFlowParticipant;
import org.finos.waltz.model.physical_specification.PhysicalSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityKind.PHYSICAL_FLOW;


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

    public void checkHasPermission(long physicalFlowId, String username) throws InsufficientPrivelegeException {
        physicalFlowService.checkHasPermission(physicalFlowId, username);
    }
}
