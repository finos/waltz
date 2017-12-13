/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
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

package com.khartec.waltz.service.actor;

import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.actor.ActorSearchDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LastUpdate;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.actor.ActorChangeCommand;
import com.khartec.waltz.model.actor.ActorCreateCommand;
import com.khartec.waltz.model.actor.ImmutableActorChangeCommand;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static java.util.stream.Collectors.toList;

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
                .withLastUpdate(LastUpdate.mkForUser(username));

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
        List<Actor> actors = search(query, EntitySearchOptions.mkForEntity(EntityKind.ACTOR));
        return actors.stream()
                .map(a -> a.entityReference())
                .collect(toList());
    }


    public List<Actor> search(String query, EntitySearchOptions options) {
        return actorSearchDao.search(query, options);
    }
}
