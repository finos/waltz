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

package org.finos.waltz.data.involvement_kind;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.involvement_kind.*;
import org.finos.waltz.schema.tables.Involvement;
import org.finos.waltz.schema.tables.Person;
import org.finos.waltz.schema.tables.records.InvolvementKindRecord;
import org.jooq.*;
import org.jooq.exception.NoDataFoundException;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkOptionalIsPresent;
import static org.finos.waltz.common.SetUtilities.filter;
import static org.finos.waltz.common.SetUtilities.fromCollection;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.PERSON;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static org.finos.waltz.schema.tables.KeyInvolvementKind.KEY_INVOLVEMENT_KIND;

@Repository
public class InvolvementKindDao {

    public static final org.finos.waltz.schema.tables.InvolvementKind involvementKind = INVOLVEMENT_KIND.as("inv_kind");

    public static final RecordMapper<Record, InvolvementKind> TO_DOMAIN_MAPPER = r -> {
        InvolvementKindRecord record = r.into(InvolvementKindRecord.class);

        return ImmutableInvolvementKind.builder()
                .id(record.getId())
                .name(record.getName())
                .description(record.getDescription())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(record.getLastUpdatedAt()))
                .lastUpdatedBy(record.getLastUpdatedBy())
                .userSelectable(record.getUserSelectable())
                .subjectKind(EntityKind.valueOf(record.getSubjectKind()))
                .permittedRole(record.getPermittedRole())
                .build();
    };


    public static final Function<InvolvementKind, InvolvementKindRecord> TO_RECORD_MAPPER = ik -> {

        InvolvementKindRecord record = new InvolvementKindRecord();
        record.setName(ik.name());
        record.setDescription(ik.description());
        record.setLastUpdatedAt(Timestamp.valueOf(ik.lastUpdatedAt()));
        record.setLastUpdatedBy(ik.lastUpdatedBy());
        record.setUserSelectable(ik.userSelectable());
        record.setSubjectKind(ik.subjectKind().name());
        record.setPermittedRole(ik.permittedRole());

        ik.externalId().ifPresent(record::setExternalId);
        ik.id().ifPresent(record::setId);

        return record;
    };


    private final DSLContext dsl;


    @Autowired
    public InvolvementKindDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<InvolvementKind> findAll() {
        return dsl
                .select(involvementKind.fields())
                .from(involvementKind)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public InvolvementKind getById(long id) {
        InvolvementKindRecord record = dsl
                .select(INVOLVEMENT_KIND.fields())
                .from(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.ID.eq(id))
                .fetchOneInto(InvolvementKindRecord.class);

        if(record == null) {
            throw new NoDataFoundException("Could not find Involvement Kind record with id: " + id);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }


    public Long create(InvolvementKindCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        InvolvementKindRecord record = dsl.newRecord(INVOLVEMENT_KIND);
        record.setName(command.name());
        record.setDescription(command.description());
        record.setLastUpdatedBy(username);
        record.setLastUpdatedAt(Timestamp.valueOf(DateTimeUtilities.nowUtc()));
        record.setSubjectKind(command.subjectKind().name());
        record.setPermittedRole(command.permittedRole());

        command.externalId().ifPresent(record::setExternalId);

        record.store();

        return record.getId();
    }


    public List<InvolvementKind> findKeyInvolvementKindsByEntityKind(EntityKind kind) {
        return dsl.select(involvementKind.fields())
                .from(involvementKind)
                .where(involvementKind.ID.in(
                        dsl.selectDistinct(KEY_INVOLVEMENT_KIND.INVOLVEMENT_KIND_ID)
                        .from(KEY_INVOLVEMENT_KIND)
                        .where(KEY_INVOLVEMENT_KIND.ENTITY_KIND.eq(kind.name()))))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean update(InvolvementKindChangeCommand command) {
        checkNotNull(command, "command cannot be null");
        checkOptionalIsPresent(command.lastUpdate(), "lastUpdate must be present");

        InvolvementKindRecord record = new InvolvementKindRecord();
        record.setId(command.id());
        record.changed(INVOLVEMENT_KIND.ID, false);

        command.name().ifPresent(change -> record.setName(change.newVal()));
        command.description().ifPresent(change -> record.setDescription(change.newVal()));
        command.externalId().ifPresent(change -> record.setExternalId(change.newVal()));
        command.userSelectable().ifPresent(change -> record.setUserSelectable(change.newVal()));
        command.permittedRole().ifPresent(change -> record.setPermittedRole(change.newVal()));

        UserTimestamp lastUpdate = command.lastUpdate().orElseThrow(() -> new IllegalStateException("InvolvementChangeCommand must have a last update timestamp"));
        record.setLastUpdatedAt(Timestamp.valueOf(lastUpdate.at()));
        record.setLastUpdatedBy(lastUpdate.by());

        return dsl.executeUpdate(record) == 1;
    }


    public boolean deleteIfNotUsed(long id) {
        return dsl
                .deleteFrom(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.ID.eq(id))
                .and(DSL.notExists(DSL
                        .select(INVOLVEMENT.fields())
                        .from(INVOLVEMENT)
                        .where(INVOLVEMENT.KIND_ID.eq(id))))
                .execute() > 0;
    }


    public Set<InvolvementKindUsageStat> loadUsageStats() {

        org.finos.waltz.schema.tables.InvolvementKind ik = INVOLVEMENT_KIND;
        Involvement inv = INVOLVEMENT;
        Person p = PERSON;

        CommonTableExpression<Record4<Long, String, Boolean, Integer>> userStatsCTE = DSL
                .name("user_stats")
                .as(DSL.select(ik.ID, inv.ENTITY_KIND, p.IS_REMOVED, DSL.countDistinct(inv.EMPLOYEE_ID).as("person_count"))
                        .from(inv)
                        .innerJoin(ik).on(ik.ID.eq(inv.KIND_ID))
                        .innerJoin(p).on(p.EMPLOYEE_ID.eq(inv.EMPLOYEE_ID))
                        .groupBy(ik.ID, inv.ENTITY_KIND, p.IS_REMOVED));

        Field<Long> invKindIdField = userStatsCTE.field(0, Long.class);
        Field<String> entityKindField = userStatsCTE.field(1, String.class);
        Field<Boolean> personIsRemovedField = userStatsCTE.field(2, Boolean.class);
        Field<Integer> personCountField = userStatsCTE.field(3, Integer.class);

        return dsl
                .with(userStatsCTE)
                .select(ik.ID,
                        ik.NAME,
                        ik.EXTERNAL_ID,
                        ik.DESCRIPTION,
                        entityKindField,
                        personIsRemovedField,
                        personCountField)
                .from(ik)
                .leftJoin(userStatsCTE).on(invKindIdField.eq(ik.ID))
                .fetch()
                .stream()
                .collect(groupingBy(
                        r -> mkRef(
                                EntityKind.INVOLVEMENT_KIND,
                                r.get(ik.ID),
                                r.get(ik.NAME),
                                r.get(ik.DESCRIPTION),
                                r.get(ik.EXTERNAL_ID)),
                        mapping(
                                r -> r.get(entityKindField) == null
                                        ? null
                                        : ImmutableStat
                                        .builder()
                                        .entityKind(EntityKind.valueOf(r.get(entityKindField)))
                                        .isCountOfRemovedPeople(r.get(personIsRemovedField))
                                        .personCount(r.get(personCountField))
                                        .build(),
                                toSet())))
                .entrySet()
                .stream()
                .map(e -> {
                    Set<InvolvementKindUsageStat.Stat> stats = filter(
                            fromCollection(e.getValue()),
                            Objects::nonNull);

                    return ImmutableInvolvementKindUsageStat
                            .builder()
                            .breakdown(stats)
                            .involvementKind(e.getKey())
                            .build();
                })
                .collect(toSet());
    }


    public InvolvementKindUsageStat loadUsageStatsForKind(Long kindId) {

        org.finos.waltz.schema.tables.InvolvementKind ik = INVOLVEMENT_KIND;
        Involvement inv = INVOLVEMENT;
        Person p = PERSON;

        SelectHavingStep<Record4<Long, String, Boolean, Integer>> qry = dsl
                .select(ik.ID, inv.ENTITY_KIND, p.IS_REMOVED, DSL.countDistinct(inv.EMPLOYEE_ID).as("person_count"))
                .from(inv)
                .innerJoin(ik).on(ik.ID.eq(inv.KIND_ID))
                .innerJoin(p).on(p.EMPLOYEE_ID.eq(inv.EMPLOYEE_ID))
                .where(ik.ID.eq(kindId))
                .groupBy(ik.ID, inv.ENTITY_KIND, p.IS_REMOVED);

        Set<InvolvementKindUsageStat.Stat> statsForKind = qry
                .fetch()
                .stream()
                .map(r -> {
                    String entityKind = r.get(inv.ENTITY_KIND);

                    return entityKind == null
                            ? null
                            : ImmutableStat
                            .builder()
                            .entityKind(EntityKind.valueOf(entityKind))
                            .isCountOfRemovedPeople(r.get(p.IS_REMOVED))
                            .personCount(r.get("person_count", Integer.class))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(toSet());

        return ImmutableInvolvementKindUsageStat.builder()
                .involvementKind(mkRef(EntityKind.INVOLVEMENT_KIND, kindId))
                .breakdown(statsForKind)
                .build();
    }

    public InvolvementKind getByExternalId(String externalId) {
        InvolvementKindRecord record = dsl
                .select(INVOLVEMENT_KIND.fields())
                .from(INVOLVEMENT_KIND)
                .where(INVOLVEMENT_KIND.EXTERNAL_ID.eq(externalId))
                .fetchOneInto(InvolvementKindRecord.class);

        if (record == null) {
            throw new NoDataFoundException("Could not find Involvement Kind record with external id: " + externalId);
        }

        return TO_DOMAIN_MAPPER.map(record);
    }
}
