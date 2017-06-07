/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.service.measurable_relationship;

import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.EntityReferenceUtilities;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.EntityRelationshipKey;
import com.khartec.waltz.model.entity_relationship.UpdateEntityRelationshipParams;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;

@Service
public class MeasurableRelationshipService {

    private final EntityRelationshipDao entityRelationshipDao;
    private final ChangeLogService changeLogService;
    private final EntityReferenceNameResolver entityReferenceNameResolver;


    @Autowired
    public MeasurableRelationshipService(EntityRelationshipDao entityRelationshipDao,
                                         EntityReferenceNameResolver entityReferenceNameResolver,
                                         ChangeLogService changeLogService) {
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        checkNotNull(entityReferenceNameResolver, "entityReferenceNameResolver cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        this.entityRelationshipDao = entityRelationshipDao;
        this.entityReferenceNameResolver = entityReferenceNameResolver;
        this.changeLogService = changeLogService;
    }


    public Collection<EntityRelationship> findForEntityReference(EntityReference entityReference) {
        checkNotNull(entityReference, "entityReference cannot be null");
        return entityRelationshipDao
                .findRelationshipsInvolving(entityReference);
    }


    public boolean remove(EntityRelationshipKey command, String username) {
        logRemoval(command, username);
        return entityRelationshipDao.remove(command);
    }


    public boolean create(EntityRelationship relationship) {
        logAddition(relationship);
        return entityRelationshipDao.create(relationship);
    }


    public boolean update(EntityRelationshipKey key, UpdateEntityRelationshipParams params, String username) {
        logUpdate(key, params, username);
        return entityRelationshipDao.update(key, params, username);
    }



    // --- helpers ---

    private void logUpdate(EntityRelationshipKey key, UpdateEntityRelationshipParams params, String username) {
        List<String> niceNames = map(
                entityReferenceNameResolver.resolve(newArrayList(key.a(), key.b())),
                EntityReferenceUtilities::pretty);

        String paramStr = "";
        paramStr += params.relationshipKind() != null
                ? " Relationship: " + params.relationshipKind()
                : "";

        paramStr += params.description() != null
                ? " Updated description"
                : "";

        writeLog(
                Operation.UPDATE,
                key.a(),
                "Updated explicit relationship to: "+ niceNames.get(1) + " with params: " + paramStr,
                username);
        writeLog(
                Operation.UPDATE,
                key.b(),
                "Updated explicit relationship to: "+ niceNames.get(0) + " with params: " + paramStr,
                username);
    }


    private void logRemoval(EntityRelationshipKey key, String username) {
        List<String> niceNames = map(
                entityReferenceNameResolver.resolve(newArrayList(key.a(), key.b())),
                EntityReferenceUtilities::pretty);

        writeLog(
                Operation.REMOVE,
                key.a(),
                "Removed explicit relationship to: " + niceNames.get(1),
                username);
        writeLog(
                Operation.REMOVE,
                key.b(),
                "Removed explicit relationship to: " + niceNames.get(0),
                username);
    }


    private void logAddition(EntityRelationship relationship) {
        List<String> niceNames = map(
                entityReferenceNameResolver.resolve(newArrayList(relationship.a(), relationship.b())),
                EntityReferenceUtilities::pretty);

        writeLog(
                Operation.ADD,
                relationship.a(),
                "Added explicit relationship to " + niceNames.get(1),
                relationship.lastUpdatedBy());
        writeLog(
                Operation.ADD,
                relationship.b(),
                "Added explicit relationship to " + niceNames.get(0),
                relationship.lastUpdatedBy());
    }


    private void writeLog(Operation op, EntityReference a, String message, String username) {
        ImmutableChangeLog logEntry = ImmutableChangeLog.builder()
                .severity(Severity.INFORMATION)
                .operation(op)
                .parentReference(a)
                .userId(username)
                .message(message)
                .build();
        changeLogService.write(logEntry);
    }

}
