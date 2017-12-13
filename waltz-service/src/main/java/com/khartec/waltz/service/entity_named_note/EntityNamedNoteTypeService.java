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

import com.khartec.waltz.data.entity_named_note.EntityNamedNoteTypeDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_named_note.EntityNamedNodeType;
import com.khartec.waltz.model.entity_named_note.EntityNamedNoteTypeChangeCommand;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

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
}
