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

package org.finos.waltz.data.architecture_required_change;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.architecture_required_change.ArchitectureRequiredChange;
import org.finos.waltz.model.architecture_required_change.ImmutableArchitectureRequiredChange;
import org.finos.waltz.schema.tables.records.ArchitectureRequiredChangeRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.finos.waltz.schema.tables.ArchitectureRequiredChange.ARCHITECTURE_REQUIRED_CHANGE;

@Repository
public class ArchitectureRequiredChangeDao {

    private static final Logger LOG = LoggerFactory.getLogger(ArchitectureRequiredChangeDao.class);

    public static final org.jooq.RecordMapper<org.jooq.Record, ArchitectureRequiredChange> TO_DOMAIN_MAPPER = record -> {
        ArchitectureRequiredChangeRecord arcRecord = record.into(ArchitectureRequiredChangeRecord.class);
        return ImmutableArchitectureRequiredChange.builder()
            .id(arcRecord.getId())
            .externalId(arcRecord.getExternalId())
            .title(arcRecord.getTitle())
            .description(arcRecord.getDescription())
            .status(arcRecord.getStatus())
            .milestoneRag(ofNullable(arcRecord.getMilestoneRag()).map(String::toString))
            .milestoneForecastDate(ofNullable(arcRecord.getMilestoneForecastDate()).map(Timestamp::toLocalDateTime))
            .externalParentId(ofNullable(arcRecord.getExternalParentId()).map(String::toString))
            .linkedEntityId(ofNullable(arcRecord.getLinkedEntityId()))
            .linkedEntityKind(ofNullable(arcRecord.getLinkedEntityKind()).map(EntityKind::valueOf))
            .createdAt(arcRecord.getCreatedAt().toLocalDateTime())
            .createdBy(arcRecord.getCreatedBy())
            .updatedAt(arcRecord.getUpdatedAt().toLocalDateTime())
            .updatedBy(arcRecord.getUpdatedBy())
            .provenance(arcRecord.getProvenance())
            .entityLifecycleStatus(EntityLifecycleStatus.valueOf(arcRecord.getEntityLifecycleStatus()))
            .build();
    };

    private final DSLContext dsl;

    @Autowired
    public ArchitectureRequiredChangeDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public ArchitectureRequiredChange getById(long id) {
        return dsl
            .select(ARCHITECTURE_REQUIRED_CHANGE.fields())
            .from(ARCHITECTURE_REQUIRED_CHANGE)
            .where(ARCHITECTURE_REQUIRED_CHANGE.ID.eq(id))
            .and(mkLifecycleCondition())
            .fetchOne(TO_DOMAIN_MAPPER);
    }

    public List<ArchitectureRequiredChange> findAll() {
        return dsl
            .select(ARCHITECTURE_REQUIRED_CHANGE.fields())
            .from(ARCHITECTURE_REQUIRED_CHANGE)
            .where(mkLifecycleCondition())
            .fetch(TO_DOMAIN_MAPPER);
    }

    public List<ArchitectureRequiredChange> findForLinkedEntity(EntityReference ref) {
        return dsl
            .select(ARCHITECTURE_REQUIRED_CHANGE.fields())
            .from(ARCHITECTURE_REQUIRED_CHANGE)
            .where(ARCHITECTURE_REQUIRED_CHANGE.LINKED_ENTITY_ID.eq(ref.id()))
            .and(ARCHITECTURE_REQUIRED_CHANGE.LINKED_ENTITY_KIND.eq(ref.kind().name()))
            .and(mkLifecycleCondition())
            .fetch(TO_DOMAIN_MAPPER);
    }

    public List<ArchitectureRequiredChange> findForLinkedEntities(List<EntityReference> changeInitiatives) {
        Map<EntityKind, List<Long>> idsByKind = changeInitiatives
            .stream()
            .map(t -> Map.entry(t.kind(), List.of(t.id())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return dsl
            .select(ARCHITECTURE_REQUIRED_CHANGE.fields())
            .from(ARCHITECTURE_REQUIRED_CHANGE)
            .where(mkForIdsByKind(idsByKind))
            .and(mkLifecycleCondition())
            .fetch(TO_DOMAIN_MAPPER);
    }

    public ArchitectureRequiredChange getByExternalId(String externalId) {
        return dsl
            .select(ARCHITECTURE_REQUIRED_CHANGE.fields())
            .from(ARCHITECTURE_REQUIRED_CHANGE)
            .where(ARCHITECTURE_REQUIRED_CHANGE.EXTERNAL_ID.eq(externalId))
            .and(mkLifecycleCondition())
            .fetchOne(TO_DOMAIN_MAPPER);
    }

    private Condition mkLifecycleCondition() {
        return ARCHITECTURE_REQUIRED_CHANGE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name());
    }

    private Condition mkSelectForLinkedEntityIds(EntityKind kind, List<Long> ids) {
        return ARCHITECTURE_REQUIRED_CHANGE.LINKED_ENTITY_ID.in(ids)
            .and(ARCHITECTURE_REQUIRED_CHANGE.LINKED_ENTITY_KIND.eq(kind.name()));
    }

    private Condition mkForIdsByKind(Map<EntityKind, List<Long>> idsByKind) {
        return idsByKind
            .entrySet()
            .stream()
            .map(entry -> mkSelectForLinkedEntityIds(entry.getKey(), entry.getValue()))
            .reduce(Condition::or)
            .orElse(DSL.falseCondition());
    }
}
