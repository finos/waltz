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

package com.khartec.waltz.service.entity_named_note;

import com.khartec.waltz.data.entity_named_note.EntityNamedNoteDao;
import com.khartec.waltz.data.entity_named_note.EntityNamedNoteTypeDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_named_note.EntityNamedNodeType;
import com.khartec.waltz.model.entity_named_note.EntityNamedNote;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkFalse;
import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class EntityNamedNoteService {

    private final EntityNamedNoteDao entityNamedNoteDao;
    private final EntityNamedNoteTypeDao entityNamedNodeTypeDao;
    private final ChangeLogService changeLogService;


    @Autowired
    public EntityNamedNoteService(EntityNamedNoteDao entityNamedNoteDao,
                                  EntityNamedNoteTypeDao entityNamedNodeTypeDao,
                                  ChangeLogService changeLogService) {
        checkNotNull(entityNamedNoteDao, "entityNamedNoteDao cannot be null");
        checkNotNull(entityNamedNodeTypeDao, "entityNamedNodeTypeDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.entityNamedNoteDao = entityNamedNoteDao;
        this.entityNamedNodeTypeDao = entityNamedNodeTypeDao;
        this.changeLogService = changeLogService;
    }


    public List<EntityNamedNote> findByEntityReference(EntityReference ref) {
        return entityNamedNoteDao.findByEntityReference(ref);
    }


    public boolean save(EntityReference ref, long namedNoteTypeId, String noteText, String username) {
        checkNotNull(ref, "ref cannot be null");

        EntityNamedNodeType type = entityNamedNodeTypeDao.getById(namedNoteTypeId);

        checkNotNull(type, "associated note type cannot be found");
        checkFalse(type.isReadOnly(), "cannot update a read-only named note");

        boolean rc = entityNamedNoteDao.save(
                ref,
                namedNoteTypeId,
                noteText,
                UserTimestamp.mkForUser(username));

        if (rc) {
            logMsg(ref, username, Operation.UPDATE, "Updated note: " + type.name());
        }
        return rc;
    }


    public boolean remove(EntityReference ref, long namedNoteTypeId, String username) {
        EntityNamedNodeType type = entityNamedNodeTypeDao.getById(namedNoteTypeId);

        if (type == null) {
            // nothing to do
            return false;
        }

        checkFalse(type.isReadOnly(), "Cannot remove a read only note");

        boolean rc = entityNamedNoteDao.remove(ref, namedNoteTypeId);

        if (rc) {
            logMsg(ref, username, Operation.REMOVE, "Removed note: " + type.name());
        }

        return rc;
    }


    public boolean remove(EntityReference ref, String username) {
        boolean rc = entityNamedNoteDao.remove(ref);

        if (rc) {
            logMsg(ref, username, Operation.REMOVE, "Removed all notes");
        }

        return rc;
    }


    private void logMsg(EntityReference ref, String username, Operation op, String msg) {
        ChangeLog logEntry = ImmutableChangeLog
                .builder()
                .userId(username)
                .parentReference(ref)
                .operation(op)
                .message(msg)
                .severity(Severity.INFORMATION)
                .build();
        changeLogService.write(logEntry);
    }

}
