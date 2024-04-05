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

package org.finos.waltz.data.involvement;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.ImmutableEntityReference;
import org.finos.waltz.model.involvement.ImmutableInvolvement;
import org.finos.waltz.model.involvement.Involvement;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.InvolvementRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.Tables.END_USER_APPLICATION;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


@Repository
public class InvolvementDao {

    private final DSLContext dsl;

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory
            .mkNameField(
                    INVOLVEMENT.ENTITY_ID,
                    INVOLVEMENT.ENTITY_KIND)
            .as("entity_name");

    private final RecordMapper<Record, Involvement> TO_MODEL_MAPPER = r -> {
        InvolvementRecord involvementRecord = r.into(InvolvementRecord.class);

        Optional<String> entityName = Optional.ofNullable(r.get(ENTITY_NAME_FIELD));

        ImmutableEntityReference entityRef = ImmutableEntityReference.builder()
                .kind(EntityKind.valueOf(involvementRecord.getEntityKind()))
                .id(involvementRecord.getEntityId())
                .name(entityName.orElse("Unknown"))
                .build();

        return ImmutableInvolvement.builder()
                .employeeId(involvementRecord.getEmployeeId())
                .kindId(involvementRecord.getKindId())
                .entityReference(entityRef)
                .isReadOnly(involvementRecord.getIsReadonly())
                .provenance(involvementRecord.getProvenance())
                .build();
    };


    private final Function<Involvement, InvolvementRecord> TO_RECORD_MAPPER = inv -> {
        InvolvementRecord record = new InvolvementRecord();
        record.setEntityKind(inv.entityReference().kind().name());
        record.setEntityId(inv.entityReference().id());
        record.setEmployeeId(inv.employeeId());
        record.setKindId(inv.kindId());
        record.setProvenance(inv.provenance());
        record.setIsReadonly(inv.isReadOnly());
        return record;
    };


    @Autowired
    public InvolvementDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");

        this.dsl = dsl;
    }


    public List<Involvement> findByEntityReference(EntityReference ref) {
        return dsl
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(ref.kind().name()))
                .and(INVOLVEMENT.ENTITY_ID.eq(ref.id()))
                .fetch(TO_MODEL_MAPPER);
    }


    /**
     * Returns a set of involvements given by a selector which returns entities the involvements
     * are associated to.
     *
     * @param genericSelector gives the criteria to determine the involvements to return
     * @return collection of involvements
     */
    public Collection<Involvement> findByGenericEntitySelector(GenericSelector genericSelector) {
        return dsl
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(INVOLVEMENT.ENTITY_ID.in(genericSelector.selector()))
                .fetch(TO_MODEL_MAPPER);
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        return dsl
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .fetch(TO_MODEL_MAPPER);
    }

    public Set<Long> findExistingInvolvementKindIdsForUser(EntityReference parentEntityRef, String username) {
        return dsl
                .select(Tables.INVOLVEMENT.KIND_ID)
                .from(Tables.INVOLVEMENT)
                .innerJoin(Tables.PERSON).on(Tables.PERSON.EMPLOYEE_ID.eq(Tables.INVOLVEMENT.EMPLOYEE_ID))
                .where(Tables.PERSON.EMAIL.eq(username)
                        .and(Tables.INVOLVEMENT.ENTITY_KIND.eq(parentEntityRef.kind().name()))
                        .and(Tables.INVOLVEMENT.ENTITY_ID.eq(parentEntityRef.id())))
                .fetchSet(Tables.INVOLVEMENT.KIND_ID);
    }


    public List<Involvement> findAllByEmployeeId(String employeeId) {
        return dsl
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(INVOLVEMENT)
                .innerJoin(PERSON_HIERARCHY).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId)
                        .or(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId)))
                .fetch(TO_MODEL_MAPPER);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        return dsl
                .selectDistinct(PERSON.fields())
                .from(PERSON)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.eq(ref.id()))
                .and(INVOLVEMENT.ENTITY_KIND.eq(ref.kind().name()))
                .where(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .fetch(PersonDao.personMapper);
    }


    public List<Person> findPeopleByGenericEntitySelector(GenericSelector selector) {
        return dsl.selectDistinct(PERSON.fields())
                .from(PERSON)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.ENTITY_ID.in(selector.selector()))
                .and(INVOLVEMENT.ENTITY_KIND.eq(selector.kind().name()))
                .where(PERSON.EMPLOYEE_ID.eq(INVOLVEMENT.EMPLOYEE_ID))
                .fetch(PersonDao.personMapper);
    }


    public Map<EntityReference, List<Person>> findPeopleByEntitySelectorAndInvolvement(
            EntityKind entityKind,
            Select<Record1<Long>> entityIdSelector,
            Set<Long> involvementKindIds) {

        return dsl.selectDistinct()
                .select(PERSON.fields())
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(PERSON)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                .where(PERSON.IS_REMOVED.isFalse()
                        .and(INVOLVEMENT.ENTITY_KIND.eq(entityKind.name())
                                .and(INVOLVEMENT.ENTITY_ID.in(entityIdSelector)
                                        .and(INVOLVEMENT.KIND_ID.in(involvementKindIds)))))
                .fetch()
                .stream()
                .collect(groupingBy(
                        r -> EntityReference.mkRef(
                                entityKind,
                                r.getValue(INVOLVEMENT.ENTITY_ID),
                                r.getValue(ENTITY_NAME_FIELD)),
                        mapping(PersonDao.personMapper::map, toList())));
    }


    public int save(Involvement involvement) {
        return ! exists(involvement)
                ? dsl.executeInsert(TO_RECORD_MAPPER.apply(involvement))
                : 0;
    }


    public int remove(Involvement involvement) {

        return exists(involvement)
                ? dsl.deleteFrom(INVOLVEMENT)
                    .where(involvementRecordSelectCondition(involvement))
                    .execute()
                : 0;
    }


    private boolean exists(Involvement involvement) {

        int count = dsl.fetchCount(DSL.select(INVOLVEMENT.fields())
                        .from(INVOLVEMENT)
                        .where(involvementRecordSelectCondition(involvement)));
        return count > 0;
    }


    private Condition involvementRecordSelectCondition(Involvement involvement) {
        Condition condition = INVOLVEMENT.ENTITY_KIND.eq(involvement.entityReference().kind().name())
                .and(INVOLVEMENT.ENTITY_ID.eq(involvement.entityReference().id()))
                .and(INVOLVEMENT.EMPLOYEE_ID.eq(involvement.employeeId()))
                .and(INVOLVEMENT.KIND_ID.eq(involvement.kindId()))
                .and(INVOLVEMENT.IS_READONLY.eq(false));
        return condition;
    }


    /**
     * Deletes a set of involvements given by a selector which returns entities the involvements
     * are associated to.
     * Removed involvements are _deleted_ from the database.
     *
     *
     * @param genericSelector gives the criteria to determine the involvements to remove
     * @return count of removed involvements
     */
    public int deleteByGenericEntitySelector(GenericSelector genericSelector) {
        return dsl
                .deleteFrom(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(INVOLVEMENT.ENTITY_ID.in(genericSelector.selector()))
                .execute();
    }


    public int countOrphanInvolvementsForKind(EntityKind entityKind) {

        Select<Record1<Long>> invalidInvolvementIds = findInvalidInvolvementsForKind(entityKind);

        return dsl
                .fetchCount(dsl
                        .select()
                        .from(INVOLVEMENT)
                        .where(INVOLVEMENT.ENTITY_ID.in(invalidInvolvementIds)
                                .and(INVOLVEMENT.ENTITY_KIND.eq(entityKind.name()))));
    }


    public int cleanupInvolvementsForKind(EntityKind entityKind) {

        Select<Record1<Long>> invalidInvolvementIds = findInvalidInvolvementsForKind(entityKind);

        return dsl
                .deleteFrom(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_ID.in(invalidInvolvementIds)
                        .and(INVOLVEMENT.ENTITY_KIND.eq(entityKind.name())))
                .execute();
    }


    private Select<Record1<Long>> findInvalidInvolvementsForKind(EntityKind entityKind) {
        switch (entityKind) {
            case CHANGE_INITIATIVE:
                return findInvalidInvolvements(EntityKind.CHANGE_INITIATIVE, CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID);
            case END_USER_APPLICATION:
                return findInvalidInvolvements(EntityKind.END_USER_APPLICATION, END_USER_APPLICATION, END_USER_APPLICATION.ID);
            default:
                throw new UnsupportedOperationException("Cannot remove involvements for kind: " + entityKind);
        }
    }


    private Select<Record1<Long>> findInvalidInvolvements(EntityKind entityKind,
                                                          Table<?> t,
                                                          TableField<? extends Record, Long> id) {
        return dsl
                .select(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .leftJoin(t).on(INVOLVEMENT.ENTITY_ID.eq(id))
                .where(INVOLVEMENT.ENTITY_KIND.eq(entityKind.name())
                        .and(id.isNull()));
    }

    public Set<Involvement> findInvolvementsByKindAndEntityKind(Long invKindId, EntityKind entityKind) {
        return dsl
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(entityKind.name())
                        .and(INVOLVEMENT.KIND_ID.eq(invKindId)))
                .fetchSet(TO_MODEL_MAPPER);
    }

    public int bulkStoreInvolvements(Set<Involvement> involvements) {
        Set<InvolvementRecord> involvementRecords = SetUtilities.map(involvements, TO_RECORD_MAPPER);
        int[] inserted = dsl.batchInsert(involvementRecords).execute();
        return IntStream.of(inserted).sum();
    }

    public Set<Involvement> findByKindIdAndEntityKind(long id, EntityKind kind) {

        return dsl
                .select(INVOLVEMENT.fields())
                .select(ENTITY_NAME_FIELD)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.KIND_ID.eq(id)
                        .and(INVOLVEMENT.ENTITY_KIND.eq(kind.name())))
                .fetchSet(TO_MODEL_MAPPER);
    }

    public int bulkDeleteInvolvements(Set<Involvement> involvements) {
        int[] removedInvolvements = involvements
                .stream()
                .map(involvement -> dsl
                        .deleteFrom(INVOLVEMENT)
                        .where(INVOLVEMENT.ENTITY_KIND.eq(involvement.entityReference().kind().name())
                                .and(INVOLVEMENT.ENTITY_ID.eq(involvement.entityReference().id()))
                                .and(INVOLVEMENT.EMPLOYEE_ID.eq(involvement.employeeId()))
                                .and(INVOLVEMENT.KIND_ID.eq(involvement.kindId()))))
                .collect(collectingAndThen(toSet(), xs -> dsl.batch(xs).execute()));
        return IntStream.of(removedInvolvements).sum();
    }
}
