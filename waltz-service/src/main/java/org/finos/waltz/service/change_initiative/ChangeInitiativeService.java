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

package org.finos.waltz.service.change_initiative;

import org.finos.waltz.model.*;
import org.finos.waltz.model.app_group.AppGroupEntry;
import org.finos.waltz.model.app_group.ImmutableAppGroupEntry;
import org.finos.waltz.service.changelog.ChangeLogService;
import org.finos.waltz.service.entity_relationship.EntityRelationshipUtilities;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.StringUtilities;
import org.finos.waltz.data.change_initiative.ChangeInitiativeDao;
import org.finos.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import org.finos.waltz.data.change_initiative.search.ChangeInitiativeSearchDao;
import org.finos.waltz.data.entity_relationship.EntityRelationshipDao;
import org.finos.waltz.model.change_initiative.ChangeInitiative;
import org.finos.waltz.model.changelog.ChangeLog;
import org.finos.waltz.model.changelog.ImmutableChangeLog;
import org.finos.waltz.model.entity_relationship.*;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.model.EntityKind.CHANGE_INITIATIVE;
import static org.finos.waltz.model.EntityReference.mkRef;

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
                .childId(command.entityReference().id())
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
                .childId(command.entityReference().id())
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

    public Collection<ChangeInitiative> findAll() {
        return changeInitiativeDao.findAll();
    }


    public List<AppGroupEntry> findEntriesForAppGroup(long groupId) {
        return relationshipDao.findRelationshipsInvolving(mkRef(EntityKind.APP_GROUP, groupId))
                .stream()
                .filter(r -> r.a().kind().equals(CHANGE_INITIATIVE) || r.b().kind().equals(CHANGE_INITIATIVE))
                .map(r -> {
                    if (r.a().kind() == CHANGE_INITIATIVE) {
                        return mkAppGroupEntryRecord(r.a(), r.provenance());
                    } else {
                        return mkAppGroupEntryRecord(r.b(), r.provenance());
                    }
                })
                .collect(Collectors.toList());
    }


    private AppGroupEntry mkAppGroupEntryRecord(EntityReference ref, String provenance) {
        return ImmutableAppGroupEntry.builder()
                .id(ref.id())
                .name(ref.name())
                .externalId(ref.externalId())
                .kind(ref.kind())
                .provenance(provenance)
                .description(ref.description())
                .entityLifecycleStatus(ref.entityLifecycleStatus())
                .isReadOnly(false)
                .build();
    }
}
