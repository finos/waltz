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

package com.khartec.waltz.service.change_unit.processors;

import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.ImmutableSetAttributeCommand;
import com.khartec.waltz.model.SetAttributeCommand;
import com.khartec.waltz.model.change_unit.ChangeAction;
import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.model.change_unit.UpdateExecutionStatusCommand;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.service.change_unit.ChangeUnitCommandProcessor;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;


@Service
public class RetireCommandProcessor implements ChangeUnitCommandProcessor {

    private final PhysicalFlowService physicalFlowService;


    @Autowired
    public RetireCommandProcessor(PhysicalFlowService physicalFlowService) {
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");

        this.physicalFlowService = physicalFlowService;
    }


    @Override
    public ChangeAction supportedAction() {
        return ChangeAction.RETIRE;
    }


    @Override
    public CommandResponse<UpdateExecutionStatusCommand> apply(UpdateExecutionStatusCommand command,
                                                               ChangeUnit changeUnit,
                                                               String userName) {
        doBasicValidation(command, changeUnit, userName);

        // update the status of the subject
        switch (changeUnit.subjectEntity().kind()) {
            case PHYSICAL_FLOW:
                return retirePhysicalFlow(command, changeUnit, userName);
            default:
                throw new IllegalArgumentException(changeUnit.subjectEntity().kind() + " is not supported for activation");
        }
    }


    private ImmutableCommandResponse<UpdateExecutionStatusCommand> retirePhysicalFlow(UpdateExecutionStatusCommand command,
                                                                                      ChangeUnit changeUnit,
                                                                                      String userName) {
        PhysicalFlow subject = physicalFlowService.getById(changeUnit.subjectEntity().id());
        checkNotNull(subject, "subject not found: " + changeUnit.subjectEntity());

        checkTrue(subject.entityLifecycleStatus().equals(changeUnit.subjectInitialStatus()),
                "current subject status does not match initial change unit status: " + subject);

        SetAttributeCommand setAttributeCommand = ImmutableSetAttributeCommand.builder()
                .entityReference(subject.entityReference())
                .name("entity_lifecycle_status")
                .value(EntityLifecycleStatus.REMOVED.name())
                .build();

        int i = physicalFlowService.updateAttribute(userName, setAttributeCommand);
        return ImmutableCommandResponse.<UpdateExecutionStatusCommand>builder()
                .entityReference(subject.entityReference())
                .originalCommand(command)
                .outcome(i == 1 ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .message("Updated status of physical flow: " + subject + " to " + EntityLifecycleStatus.REMOVED)
                .build();
    }
}
