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

package org.finos.waltz.service.change_unit.processors;

import org.finos.waltz.service.change_unit.ChangeUnitCommandProcessor;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.physical_flow.PhysicalFlowService;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.ImmutableSetAttributeCommand;
import org.finos.waltz.model.SetAttributeCommand;
import org.finos.waltz.model.change_unit.ChangeAction;
import org.finos.waltz.model.change_unit.ChangeUnit;
import org.finos.waltz.model.change_unit.UpdateExecutionStatusCommand;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.command.ImmutableCommandResponse;
import org.finos.waltz.model.physical_flow.PhysicalFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;


@Service
public class ActivateCommandProcessor implements ChangeUnitCommandProcessor {

    private final ChangeLogService changeLogService;
    private final PhysicalFlowService physicalFlowService;


    @Autowired
    public ActivateCommandProcessor(ChangeLogService changeLogService,
                                    PhysicalFlowService physicalFlowService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.changeLogService = changeLogService;
        this.physicalFlowService = physicalFlowService;
    }


    @Override
    public ChangeAction supportedAction() {
        return ChangeAction.ACTIVATE;
    }


    @Override
    public CommandResponse<UpdateExecutionStatusCommand> apply(UpdateExecutionStatusCommand command,
                                                               ChangeUnit changeUnit,
                                                               String userName) {
        doBasicValidation(command, changeUnit, userName);

        // update the status of the subject
        switch (changeUnit.subjectEntity().kind()) {
            case PHYSICAL_FLOW:
                return activatePhysicalFlow(command, changeUnit, userName);
            default:
                throw new IllegalArgumentException(changeUnit.subjectEntity().kind() + " is not supported for activation");
        }
    }


    private ImmutableCommandResponse<UpdateExecutionStatusCommand> activatePhysicalFlow(UpdateExecutionStatusCommand command,
                                                                                        ChangeUnit changeUnit,
                                                                                        String userName) {
        doBasicValidation(command, changeUnit, userName);

        PhysicalFlow subject = physicalFlowService.getById(changeUnit.subjectEntity().id());
        checkNotNull(subject, "subject not found: " + changeUnit.subjectEntity());

        checkTrue(subject.entityLifecycleStatus().equals(changeUnit.subjectInitialStatus()),
                "current subject status does not initial change unit status: " + subject);

        SetAttributeCommand setAttributeCommand = ImmutableSetAttributeCommand.builder()
                .entityReference(subject.entityReference())
                .name("entity_lifecycle_status")
                .value(EntityLifecycleStatus.ACTIVE.name())
                .build();

        int i = physicalFlowService.updateAttribute(userName, setAttributeCommand);
        return ImmutableCommandResponse.<UpdateExecutionStatusCommand>builder()
                .entityReference(subject.entityReference())
                .originalCommand(command)
                .outcome(i == 1 ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .message("Updated status of physical flow: " + subject + " to " + EntityLifecycleStatus.ACTIVE)
                .build();
    }
}
