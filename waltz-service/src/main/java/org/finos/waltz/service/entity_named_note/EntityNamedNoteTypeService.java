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

package org.finos.waltz.service.entity_named_note;

import org.finos.waltz.common.Checks;
import org.finos.waltz.data.entity_named_note.EntityNamedNoteTypeDao;
import org.finos.waltz.model.*;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.entity_named_note.EntityNamedNodeType;
import org.finos.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static org.finos.waltz.common.Checks.checkNotNull;

@Service
public class EntityNamedNoteTypeService {

    private final EntityNamedNoteTypeDao entityNamedNoteTypeDao;
    private final ChangeLogService changeLogService;

    
    @Autowired
    public EntityNamedNoteTypeService(EntityNamedNoteTypeDao entityNamedNoteTypeDao,
                                      ChangeLogService changeLogService) {
        checkNotNull(entityNamedNoteTypeDao, "entityNamedNoteTypeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.entityNamedNoteTypeDao = entityNamedNoteTypeDao;
        this.changeLogService = changeLogService;
    }

    
    public List<EntityNamedNodeType> findAll() {
        return entityNamedNoteTypeDao.findAll();
    }


    public boolean removeById(Long id, String username) {
        boolean rc = entityNamedNoteTypeDao.removeById(id);
        if (rc) {
            logMsg(id, username, Operation.REMOVE, "Type removed");
        }
        return rc;
    }


    public long create(EntityNamedNoteTypeChangeCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        long id = entityNamedNoteTypeDao.create(command);
        logMsg(id, username, Operation.ADD, "Type created: " + command);
        return id;
    }


    public boolean update(long id, EntityNamedNoteTypeChangeCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        boolean rc = entityNamedNoteTypeDao.update(id, command);
        if (rc) {
            logMsg(id, username, Operation.UPDATE, "Type updated: " + command);
        }
        return rc;
    }


    private void logMsg(Long id, String username, Operation op, String msg) {
        ChangeLog logEntry = ImmutableChangeLog
                .builder()
                .userId(username)
                .parentReference(EntityReference.mkRef(EntityKind.ENTITY_NAMED_NOTE_TYPE, id))
                .operation(op)
                .message(msg)
                .severity(Severity.INFORMATION)
                .build();
        changeLogService.write(logEntry);
    }

    public EntityNamedNodeType getByExternalId(String externalId) {
        Checks.checkNotNull(externalId, "External id cannot be null");
        return entityNamedNoteTypeDao.getByExternalId(externalId);
    }


    public Set<EntityWithOperations<EntityNamedNodeType>> findForRefAndUser(EntityReference ref,
                                                                            String username) {
        return entityNamedNoteTypeDao.findForRefAndUser(ref, username);
    }
}
