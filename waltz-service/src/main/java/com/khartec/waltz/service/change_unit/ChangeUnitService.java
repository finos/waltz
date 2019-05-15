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

package com.khartec.waltz.service.change_unit;

import com.khartec.waltz.data.change_unit.ChangeUnitDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.change_unit.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.service.changelog.ChangeLogService;
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
    private final Map<ChangeAction, ChangeUnitCommandProcessor> processorsByChangeAction;


    @Autowired
    public ChangeUnitService(ChangeLogService changeLogService,
                             ChangeUnitDao dao,
                             List<ChangeUnitCommandProcessor> processors) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(dao, "dao cannot be null");
        checkNotNull(processors, "processors cannot be null");

        this.changeLogService = changeLogService;
        this.dao = dao;

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


    public CommandResponse<UpdateExecutionStatusCommand>updateExecutionStatus(UpdateExecutionStatusCommand command,
                                                                              String userName) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(userName, "userName cannot be null");

        ImmutableUpdateExecutionStatusCommand updateCommand = ImmutableUpdateExecutionStatusCommand
                .copyOf(command)
                .withLastUpdate(LastUpdate.mkForUser(userName));

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
