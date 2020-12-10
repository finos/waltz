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

package com.khartec.waltz.data.change_set;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.change_set.ChangeSet;
import com.khartec.waltz.model.change_set.ImmutableChangeSet;
import com.khartec.waltz.schema.tables.records.ChangeSetRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.CHANGE_SET;
import static com.khartec.waltz.schema.Tables.INVOLVEMENT;


@Repository
public class ChangeSetDao {

    private final DSLContext dsl;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            CHANGE_SET.PARENT_ENTITY_ID,
            CHANGE_SET.PARENT_ENTITY_KIND,
            newArrayList(EntityKind.values()))
            .as("entity_name");

    public static final RecordMapper<Record, ChangeSet> TO_DOMAIN_MAPPER = r -> {
        ChangeSetRecord record = r.into(ChangeSetRecord.class);

        Optional<LocalDateTime> plannedDate = Optional
                .ofNullable(record.getPlannedDate())
                .map(Timestamp::toLocalDateTime);

        return ImmutableChangeSet.builder()
                .id(record.getId())
                .parentEntity(mkParentRef(r))
                .plannedDate(plannedDate)
                .entityLifecycleStatus(EntityLifecycleStatus.valueOf(record.getEntityLifecycleStatus()))
                .name(record.getName())
                .description(record.getDescription())
                .lastUpdatedAt(toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .build();
    };


    public static final Function<ChangeSet, ChangeSetRecord> TO_RECORD_MAPPER = changeSet -> {

        ChangeSetRecord record = new ChangeSetRecord();
        changeSet.id().ifPresent(record::setId);
        changeSet.parentEntity().ifPresent(ref -> record.setParentEntityKind(ref.kind().name()));
        changeSet.parentEntity().ifPresent(ref -> record.setParentEntityId(ref.id()));
        record.setPlannedDate(changeSet.plannedDate().map(t -> Timestamp.valueOf(t)).orElse(null));
        record.setEntityLifecycleStatus(changeSet.entityLifecycleStatus().name());
        record.setName(changeSet.name());
        record.setDescription(changeSet.description());
        record.setLastUpdatedAt(Timestamp.valueOf(changeSet.lastUpdatedAt()));
        record.setLastUpdatedBy(changeSet.lastUpdatedBy());
        record.setExternalId(changeSet.externalId().orElse(null));
        record.setProvenance(changeSet.provenance());

        return record;
    };


    @Autowired
    public ChangeSetDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public ChangeSet getById(long id) {
        return dsl
                .select(CHANGE_SET.fields())
                .select(ENTITY_NAME_FIELD)
                .from(CHANGE_SET)
                .where(CHANGE_SET.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<ChangeSet> findByParentRef(EntityReference ref) {
        return dsl.select(CHANGE_SET.fields())
                .select(ENTITY_NAME_FIELD)
                .from(CHANGE_SET)
                .where(CHANGE_SET.PARENT_ENTITY_ID.eq(ref.id()))
                .and(CHANGE_SET.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeSet> findBySelector(Select<Record1<Long>> selector) {
        return dsl.select(CHANGE_SET.fields())
                .select(ENTITY_NAME_FIELD)
                .from(CHANGE_SET)
                .where(CHANGE_SET.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ChangeSet> findByPerson(String employeeId) {
        return dsl.select(CHANGE_SET.fields())
                .select(ENTITY_NAME_FIELD)
                .from(CHANGE_SET)
                .innerJoin(INVOLVEMENT).on(INVOLVEMENT.ENTITY_ID.eq(CHANGE_SET.ID))
                    .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.CHANGE_SET.name()))
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    private static Optional<EntityReference> mkParentRef(Record r) {
        ChangeSetRecord record = r.into(ChangeSetRecord.class);
        if(record.getParentEntityKind() != null && record.getParentEntityId() != null) {
            return Optional.of(EntityReference.mkRef(
                    EntityKind.valueOf(record.getParentEntityKind()),
                    record.getParentEntityId(),
                    r.getValue(ENTITY_NAME_FIELD)));
        } else {
            return Optional.empty();
        }
    }

}
