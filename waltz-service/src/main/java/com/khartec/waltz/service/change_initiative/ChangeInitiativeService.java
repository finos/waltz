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

package com.khartec.waltz.service.change_initiative;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import com.khartec.waltz.data.change_initiative.search.ChangeInitiativeSearchDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_relationship.*;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.entity_relationship.EntityRelationshipUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.CHANGE_INITIATIVE;
import static com.khartec.waltz.model.EntityReference.mkRef;

@Service
public class ChangeInitiativeService {

    private final ChangeInitiativeDao changeInitiativeDao;
    private final ChangeInitiativeSearchDao searchDao;
    private final EntityRelationshipDao relationshipDao;
    private final ChangeLogService changeLogService;
    private final ChangeInitiativeIdSelectorFactory changeInitiativeIdSelectorFactory = new ChangeInitiativeIdSelectorFactory();

    @Autowired
    public ChangeInitiativeService(ChangeInitiativeDao changeInitiativeDao,
                                   ChangeInitiativeSearchDao searchDao,
                                   EntityRelationshipDao relationshipDao,
                                   ChangeLogService changeLogService)
    {
        checkNotNull(changeInitiativeDao, "changeInitiativeDao cannot be null");
        checkNotNull(searchDao, "searchDao cannot be null");
        checkNotNull(relationshipDao, "relationshipDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.changeInitiativeDao = changeInitiativeDao;
        this.searchDao = searchDao;
        this.relationshipDao = relationshipDao;
        this.changeLogService = changeLogService;
    }


    public ChangeInitiative getById(Long id) {
        return changeInitiativeDao.getById(id);
    }



    public Collection<ChangeInitiative> findForSelector(IdSelectionOptions selectionOptions) {
        return changeInitiativeDao.findForSelector(changeInitiativeIdSelectorFactory.apply(selectionOptions));
    }


    public Collection<ChangeInitiative> findHierarchyForSelector(IdSelectionOptions selectionOptions) {
        return changeInitiativeDao.findHierarchyForSelector(changeInitiativeIdSelectorFactory.apply(selectionOptions));
    }


    public Collection<ChangeInitiative> search(String query) {
        if (StringUtilities.isEmpty(query)) {
            return Collections.emptyList();
        }
        return search(EntitySearchOptions.mkForEntity(CHANGE_INITIATIVE, query));
    }


    public Collection<ChangeInitiative> search(EntitySearchOptions options) {
        return searchDao.search(options);
    }


    public Collection<EntityRelationship> getRelatedEntitiesForId(long id) {
        EntityReference ref = mkRef(CHANGE_INITIATIVE, id);
        return relationshipDao.findRelationshipsInvolving(ref);
    }


    public boolean addEntityRelationship(long changeInitiativeId,
                                         EntityRelationshipChangeCommand command,
                                         String username) {
        checkNotNull(command, "command cannot be null");
        EntityRelationshipKey key = mkEntityRelationshipKey(changeInitiativeId, command, true);

        EntityRelationship entityRelationship = ImmutableEntityRelationship.builder()
                .a(key.a())
                .b(key.b())
                .relationship(key.relationshipKind())
                .lastUpdatedBy(username)
                .lastUpdatedAt(DateTimeUtilities.nowUtc())
                .build();

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(String.format(
                        "Relationship to %s (%s) added",
                        command.entityReference().name().orElse(""),
                        command.entityReference().kind().name() + "/" + command.entityReference().id()))
                .parentReference(mkRef(CHANGE_INITIATIVE, changeInitiativeId))
                .userId(username)
                .childKind(command.entityReference().kind())
                .severity(Severity.INFORMATION)
                .operation(Operation.ADD)
                .build();

        changeLogService.write(logEntry);

        return relationshipDao.save(entityRelationship) == 1;
    }


    public boolean removeEntityRelationship(long changeInitiativeId,
                                            EntityRelationshipChangeCommand command,
                                            String username) {
        checkNotNull(command, "command cannot be null");
        EntityRelationshipKey key = mkEntityRelationshipKey(changeInitiativeId, command, false);

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .message(String.format(
                        "Relationship to %s (%s) removed",
                        command.entityReference().name().orElse(""),
                        command.entityReference().kind().name() + "/" + command.entityReference().id()))
                .parentReference(mkRef(CHANGE_INITIATIVE, changeInitiativeId))
                .userId(username)
                .childKind(command.entityReference().kind())
                .severity(Severity.INFORMATION)
                .operation(Operation.REMOVE)
                .build();

        changeLogService.write(logEntry);
        return relationshipDao.remove(key);
    }


    private EntityRelationshipKey mkEntityRelationshipKey(long changeInitiativeId,
                                                          EntityRelationshipChangeCommand command,
                                                          boolean validate) {
        EntityReference entityReference = command.entityReference();
        RelationshipKind relationship = command.relationship();

        return EntityRelationshipUtilities.mkEntityRelationshipKey(
            mkRef(CHANGE_INITIATIVE, changeInitiativeId),
            entityReference,
            relationship,
            validate)
            .orElseThrow(() -> new IllegalArgumentException(String.format(
                    "Could not build a valid relationship for kind: %s between %s and %s",
                    relationship,
                    CHANGE_INITIATIVE,
                    entityReference.kind()
            )));
    }

    public Collection<ChangeInitiative> findByExternalId(String externalId) {
        return changeInitiativeDao.findByExternalId(externalId);
    }
}
