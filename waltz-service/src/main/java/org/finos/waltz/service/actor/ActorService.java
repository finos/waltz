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

package org.finos.waltz.service.actor;

import org.finos.waltz.data.actor.ActorDao;
import org.finos.waltz.data.actor.ActorSearchDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.actor.Actor;
import org.finos.waltz.model.actor.ActorChangeCommand;
import org.finos.waltz.model.actor.ActorCreateCommand;
import org.finos.waltz.model.actor.ImmutableActorChangeCommand;
import org.finos.waltz.model.command.CommandOutcome;
import org.finos.waltz.model.command.CommandResponse;
import org.finos.waltz.model.command.ImmutableCommandResponse;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.StringUtilities.isEmpty;

@Service
public class ActorService {

    private final ActorDao actorDao;
    private final ActorSearchDao actorSearchDao;


    @Autowired
    public ActorService(ActorDao actorDao, ActorSearchDao actorSearchDao) {
        checkNotNull(actorDao, "actorDao cannot be null");
        checkNotNull(actorSearchDao, "actorSearchDao cannot be null");

        this.actorDao = actorDao;
        this.actorSearchDao = actorSearchDao;
    }


    public List<Actor> findAll() {
        return actorDao.findAll();
    }


    public Actor getById(long id) {
        return actorDao.getById(id);
    }


    public Long create(ActorCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        return actorDao.create(command, username);
    }


    public CommandResponse<ActorChangeCommand> update(ActorChangeCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        ImmutableActorChangeCommand updateCommand = ImmutableActorChangeCommand
                .copyOf(command)
                .withLastUpdate(UserTimestamp.mkForUser(username));

        boolean success = actorDao.update(updateCommand);
        return ImmutableCommandResponse.<ActorChangeCommand>builder()
                .originalCommand(command)
                .entityReference(EntityReference.mkRef(EntityKind.ACTOR, command.id()))
                .outcome(success ? CommandOutcome.SUCCESS : CommandOutcome.FAILURE)
                .build();
    }


    public boolean delete(long id) {
        return actorDao.deleteIfNotUsed(id);
    }


    public List<EntityReference> search(String query) {
        if (isEmpty(query)) return emptyList();
        List<Actor> actors = search(EntitySearchOptions.mkForEntity(EntityKind.ACTOR, query));
        return actors.stream()
                .map(Actor::entityReference)
                .collect(toList());
    }


    public List<Actor> search(EntitySearchOptions options) {
        return actorSearchDao.search(options);
    }
}
