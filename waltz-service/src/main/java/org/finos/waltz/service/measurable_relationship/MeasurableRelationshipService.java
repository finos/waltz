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

package org.finos.waltz.service.measurable_relationship;

import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.data.EntityReferenceNameResolver;
import org.finos.waltz.data.entity_relationship.EntityRelationshipDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.Operation;
import org.finos.waltz.model.Severity;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.entity_relationship.EntityRelationship;
import org.finos.waltz.model.entity_relationship.EntityRelationshipKey;
import org.finos.waltz.model.entity_relationship.ImmutableEntityRelationship;
import org.finos.waltz.model.entity_relationship.UpdateEntityRelationshipParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.ListUtilities.newArrayList;

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


    public Map<EntityKind, Integer> tallyForEntityReference(EntityReference entityReference) {
        checkNotNull(entityReference, "entityReference cannot be null");
        return entityRelationshipDao
                .tallyRelationshipsInvolving(entityReference);
    }


    public boolean remove(EntityRelationshipKey command, String username) {
        boolean result = entityRelationshipDao.remove(command);
        if (result) {
            logRemoval(command, username);
        }
        return result;
    }


    public boolean create(String userName,
                          EntityReference entityRefA,
                          EntityReference entityRefB,
                          String relationshipKind,
                          String description) {

        EntityRelationship relationship = ImmutableEntityRelationship.builder()
                .a(entityRefA)
                .b(entityRefB)
                .relationship(relationshipKind)
                .description(description)
                .lastUpdatedBy(userName)
                .build();

        boolean result = entityRelationshipDao.create(relationship);
        if (result) {
            logAddition(relationship);
        }
        return result;
    }


    public boolean update(EntityRelationshipKey key, UpdateEntityRelationshipParams params, String username) {
        boolean result = entityRelationshipDao.update(key, params, username);
        if (result) {
            logUpdate(key, params, username);
        }
        return result;
    }


    // --- helpers ---

    private void logUpdate(EntityRelationshipKey key, UpdateEntityRelationshipParams params, String username) {
        List<String> niceNames = resolveNames(
                key.a(),
                key.b());

        String paramStr = "";
        paramStr += params.relationshipKind() != null
                ? " Relationship: " + params.relationshipKind()
                : "";

        paramStr += params.description() != null
                ? " Updated description"
                : "";

        String msg = format(
                "Updated explicit relationship from: '%s', to: '%s', with params: '%s'",
                niceNames.get(0),
                niceNames.get(1),
                paramStr);

        writeLog(
                Operation.UPDATE,
                key.a(),
                msg,
                username);
        writeLog(
                Operation.UPDATE,
                key.b(),
                msg,
                username);
    }


    private void logRemoval(EntityRelationshipKey key, String username) {
        List<String> niceNames = resolveNames(
                key.a(),
                key.b());

        String msg = format(
                "Removed explicit relationship: '%s' from: '%s', to: '%s'",
                key.relationshipKind(),
                niceNames.get(0),
                niceNames.get(1));

        writeLog(
                Operation.REMOVE,
                key.a(),
                msg,
                username);
        writeLog(
                Operation.REMOVE,
                key.b(),
                msg,
                username);
    }


    private void logAddition(EntityRelationship relationship) {
        List<String> niceNames = resolveNames(
                relationship.a(),
                relationship.b());

        String msg = format(
                "Added explicit relationship: '%s' from: '%s', to: '%s'",
                relationship.relationship(),
                niceNames.get(0),
                niceNames.get(1));

        writeLog(
                Operation.ADD,
                relationship.a(),
                msg,
                relationship.lastUpdatedBy());
        writeLog(
                Operation.ADD,
                relationship.b(),
                msg,
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


    private List<String> resolveNames(EntityReference... refs) {
        return map(
                entityReferenceNameResolver.resolve(newArrayList(refs)),
                r -> r.name().orElse("?"));
    }

}
