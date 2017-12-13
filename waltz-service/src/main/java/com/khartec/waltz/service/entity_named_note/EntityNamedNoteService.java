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

package com.khartec.waltz.service.entity_named_note;

import com.khartec.waltz.data.entity_named_note.EntityNamedNoteDao;
import com.khartec.waltz.data.entity_named_note.EntityNamedNoteTypeDao;
import com.khartec.waltz.model.*;
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
                LastUpdate.mkForUser(username));

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
