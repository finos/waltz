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

package com.khartec.waltz.service.change_unit;

import com.khartec.waltz.data.change_unit.ChangeUnitDao;
import com.khartec.waltz.data.change_unit.ChangeUnitIdSelectorFactory;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.change_unit.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.util.stream.Collectors.toMap;


@Service
public class ChangeUnitService {

    private final ChangeLogService changeLogService;
    private final ChangeUnitDao dao;
    private final ChangeUnitIdSelectorFactory changeUnitIdSelectorFactory = new ChangeUnitIdSelectorFactory();
    private final Map<ChangeAction, ChangeUnitCommandProcessor> processorsByChangeAction;


    @Autowired
    public ChangeUnitService(ChangeLogService changeLogService,
                             ChangeUnitDao changeUnitDao,
                             List<ChangeUnitCommandProcessor> processors) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(changeUnitDao, "changeUnitDao cannot be null");
        checkNotNull(processors, "processors cannot be null");

        this.changeLogService = changeLogService;
        this.dao = changeUnitDao;

        processorsByChangeAction = processors
                .stream()
                .collect(toMap(t -> t.supportedAction(), t -> t));
    }


    public ChangeUnit getById(long id) {
        return dao.getById(id);
    }


    public List<ChangeUnit> findBySubjectRef(EntityReference ref) {
        return dao.findBySubjectRef(ref);
    }


    public List<ChangeUnit> findByChangeSetId(long id) {
        return dao.findByChangeSetId(id);
    }


    public List<ChangeUnit> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = changeUnitIdSelectorFactory.apply(options);
        return dao.findBySelector(selector);
    }


    public CommandResponse<UpdateExecutionStatusCommand>updateExecutionStatus(UpdateExecutionStatusCommand command,
                                                                              String userName) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(userName, "userName cannot be null");

        ImmutableUpdateExecutionStatusCommand updateCommand = ImmutableUpdateExecutionStatusCommand
                .copyOf(command)
                .withLastUpdate(UserTimestamp.mkForUser(userName));

        // if execute - need to affect changes
        if(command.executionStatus().newVal() == ExecutionStatus.COMPLETE) {
            executeChangeUnit(command, userName);
        }

        boolean success = dao.updateExecutionStatus(updateCommand);

        if(success) {
            changeLogService.write(
                    ImmutableChangeLog.builder()
                            .operation(Operation.UPDATE)
                            .userId(userName)
                            .parentReference(mkRef(EntityKind.CHANGE_UNIT, command.id()))
                            .message("Change Unit Id: " + command.id()
                                    + " execution status changed to " + command.executionStatus())
                            .build());
        }

        return ImmutableCommandResponse.<UpdateExecutionStatusCommand>builder()
                .originalCommand(command)
                .entityReference(mkRef(EntityKind.CHANGE_UNIT, command.id()))
                .outcome(success ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .build();
    }


    private CommandResponse<UpdateExecutionStatusCommand> executeChangeUnit(UpdateExecutionStatusCommand command, String userName) {
        // get the change unit
        ChangeUnit changeUnit = getById(command.id());
        checkNotNull(changeUnit, "changeUnit with id: " + command.id() + " not found");

        ChangeUnitCommandProcessor commandProcessor = getCommandProcessor(changeUnit.action());
        return commandProcessor.apply(command, changeUnit, userName);
    }


    private ChangeUnitCommandProcessor getCommandProcessor(ChangeAction changeAction) {
        ChangeUnitCommandProcessor commandProcessor = processorsByChangeAction.get(changeAction);
        checkNotNull(commandProcessor, "Cannot find processor for action: " + changeAction);
        return commandProcessor;
    }

}
