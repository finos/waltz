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

package org.finos.waltz.service.involvement_kind;

import org.finos.waltz.data.involvement_kind.InvolvementKindDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.command.ImmutableCommandResponse;
import org.finos.waltz.model.involvement_kind.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.FunctionUtilities.time;

@Service
public class InvolvementKindService {

    private final InvolvementKindDao involvementKindDao;


    @Autowired
    public InvolvementKindService(InvolvementKindDao involvementKindDao) {
        checkNotNull(involvementKindDao, "involvementKindDao cannot be null");

        this.involvementKindDao = involvementKindDao;
    }


    public List<InvolvementKind> findAll() {
        return involvementKindDao.findAll();
    }


    public InvolvementKind getById(long id) {
        return involvementKindDao.getById(id);
    }


    public List<InvolvementKind> findKeyInvolvementKindsByEntityKind(EntityKind entityKind) {
        checkNotNull(entityKind, "entityKind cannot be null");
        return time("IKS.findKeyInvolvementKindsByEntityKind",
                () -> involvementKindDao.findKeyInvolvementKindsByEntityKind(entityKind));
    }


    public Long create(InvolvementKindCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        return involvementKindDao.create(command, username);
    }


    public CommandResponse<InvolvementKindChangeCommand> update(InvolvementKindChangeCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        ImmutableInvolvementKindChangeCommand updateCommand = ImmutableInvolvementKindChangeCommand
                .copyOf(command)
                .withLastUpdate(UserTimestamp.mkForUser(username));

        boolean success = involvementKindDao.update(updateCommand);
        return ImmutableCommandResponse.<InvolvementKindChangeCommand>builder()
                .originalCommand(command)
                .entityReference(EntityReference.mkRef(EntityKind.INVOLVEMENT_KIND, command.id()))
                .outcome(success ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .build();
    }


    public boolean delete(long id) {
        return involvementKindDao.deleteIfNotUsed(id);
    }


    public Set<InvolvementKindUsageStat> loadUsageStats() {
        return involvementKindDao.
                loadUsageStats();
    }

}
