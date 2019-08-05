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

package com.khartec.waltz.service.change_unit.processors;

import com.khartec.waltz.model.attribute_change.AttributeChange;
import com.khartec.waltz.model.change_unit.ChangeAction;
import com.khartec.waltz.model.change_unit.ChangeUnit;
import com.khartec.waltz.model.change_unit.UpdateExecutionStatusCommand;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.service.attribute_change.AttributeChangeService;
import com.khartec.waltz.service.change_unit.AttributeChangeCommandProcessor;
import com.khartec.waltz.service.change_unit.ChangeUnitCommandProcessor;
import com.khartec.waltz.service.physical_flow.PhysicalFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static java.util.stream.Collectors.toMap;


@Service
public class ModifyCommandProcessor implements ChangeUnitCommandProcessor {

    private final AttributeChangeService attributeChangeService;
    private final PhysicalFlowService physicalFlowService;
    private final Map<String, AttributeChangeCommandProcessor> processorsByAttribute;


    @Autowired
    public ModifyCommandProcessor(AttributeChangeService attributeChangeService,
                                  PhysicalFlowService physicalFlowService,
                                  List<AttributeChangeCommandProcessor> processors) {
        checkNotNull(attributeChangeService, "attributeChangeService cannot be null");
        checkNotNull(physicalFlowService, "physicalFlowService cannot be null");
        checkNotNull(processors, "processors cannot be null");

        this.attributeChangeService = attributeChangeService;
        this.physicalFlowService = physicalFlowService;
        this.processorsByAttribute = processors
                .stream()
                .collect(toMap(AttributeChangeCommandProcessor::supportedAttribute, t -> t));
    }


    @Override
    public ChangeAction supportedAction() {
        return ChangeAction.MODIFY;
    }


    @Override
    public CommandResponse<UpdateExecutionStatusCommand> apply(UpdateExecutionStatusCommand command,
                                                               ChangeUnit changeUnit,
                                                               String userName) {
        doBasicValidation(command, changeUnit, userName);

        // update the status of the subject
        switch (changeUnit.subjectEntity().kind()) {
            case PHYSICAL_FLOW:
                return modifyPhysicalFlow(command, changeUnit, userName);
            default:
                throw new IllegalArgumentException(changeUnit.subjectEntity().kind() + " is not supported for modification");
        }
    }


    private CommandResponse<UpdateExecutionStatusCommand> modifyPhysicalFlow(UpdateExecutionStatusCommand command,
                                                                             ChangeUnit changeUnit,
                                                                             String userName) {
        doBasicValidation(command, changeUnit, userName);

        PhysicalFlow subject = physicalFlowService.getById(changeUnit.subjectEntity().id());
        checkNotNull(subject, "subject not found: " + changeUnit.subjectEntity());

        checkTrue(subject.entityLifecycleStatus().equals(changeUnit.subjectInitialStatus()),
                "current subject status does not match initial change unit status: " + subject);

        // fetch attribute changes
        List<AttributeChange> attributeChanges = attributeChangeService.findByChangeUnitId(changeUnit.id().get());

        boolean success = attributeChanges
                .stream()
                .map(a -> processAttributeChange(a, changeUnit, userName))
                .allMatch(a -> a == true);

        return ImmutableCommandResponse.<UpdateExecutionStatusCommand>builder()
                .entityReference(subject.entityReference())
                .originalCommand(command)
                .outcome(success ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .message("Modified physical flow: " + subject + " attributes")
                .build();

    }


    private boolean processAttributeChange(AttributeChange attributeChange, ChangeUnit changeUnit, String userName) {
        AttributeChangeCommandProcessor commandProcessor = getCommandProcessor(attributeChange.name());
        return commandProcessor.apply(attributeChange, changeUnit, userName);
    }


    private AttributeChangeCommandProcessor getCommandProcessor(String attributeName) {
        AttributeChangeCommandProcessor commandProcessor = processorsByAttribute.get(attributeName);
        checkNotNull(commandProcessor, "Cannot find processor for attribute: " + attributeName);
        return commandProcessor;
    }
}
