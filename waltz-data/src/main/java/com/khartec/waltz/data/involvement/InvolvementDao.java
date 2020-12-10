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

package com.khartec.waltz.data.involvement;

import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.end_user_app.EndUserAppDao;
import com.khartec.waltz.data.person.PersonDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.enduserapp.EndUserApplication;
import com.khartec.waltz.model.involvement.ImmutableInvolvement;
import com.khartec.waltz.model.involvement.Involvement;
import com.khartec.waltz.model.person.Person;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;
import static java.util.stream.Collectors.*;


@Repository
public class InvolvementDao {

    private final DSLContext dsl;

    private final RecordMapper<Record, Involvement> TO_MODEL_MAPPER = r -> {
        InvolvementRecord involvementRecord = r.into(InvolvementRecord.class);
        return ImmutableInvolvement.builder()
                .employeeId(involvementRecord.getEmployeeId())
                .kindId(involvementRecord.getKindId())
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.valueOf(involvementRecord.getEntityKind()))
                        .id(involvementRecord.getEntityId())
                        .build())
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
        return dsl.select()
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
                .selectFrom(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(INVOLVEMENT.ENTITY_ID.in(genericSelector.selector()))
                .fetch(TO_MODEL_MAPPER);
    }


    public List<Involvement> findByEmployeeId(String employeeId) {
        return dsl.select()
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .fetch(TO_MODEL_MAPPER);
    }


    public List<Involvement> findAllByEmployeeId(String employeeId) {
        return dsl.select()
                .from(INVOLVEMENT)
                .innerJoin(PERSON_HIERARCHY).on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId)
                        .or(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId)))
                .fetch(TO_MODEL_MAPPER);
    }


    @Deprecated
    public List<Application> findDirectApplicationsByEmployeeId(String employeeId) {
        return dsl.select()
                .from(APPLICATION)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.EMPLOYEE_ID.eq(employeeId))
                .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(APPLICATION.ID.eq(INVOLVEMENT.ENTITY_ID))
                .and(IS_ACTIVE)
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }


    @Deprecated
    public List<Application> findAllApplicationsByEmployeeId(String employeeId) {
        SelectOrderByStep<Record1<String>> employeeIds = DSL
                .selectFrom(DSL
                    .selectDistinct(PERSON_HIERARCHY.EMPLOYEE_ID)
                    .from(PERSON_HIERARCHY)
                    .where(PERSON_HIERARCHY.MANAGER_ID.eq(employeeId))
                    .union(DSL.select(DSL.value(employeeId))
                            .from(PERSON_HIERARCHY)).asTable());

        SelectConditionStep<Record1<Long>> applicationIds = DSL
                .selectDistinct(INVOLVEMENT.ENTITY_ID)
                .from(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND
                        .eq(EntityKind.APPLICATION.name())
                        .and(INVOLVEMENT.EMPLOYEE_ID.in(employeeIds)));

        SelectConditionStep<Record> query = dsl
                .select(APPLICATION.fields())
                .from(APPLICATION)
                .where(APPLICATION.ID.in(applicationIds))
                .and(IS_ACTIVE);

        return query
                .fetch(ApplicationDao.TO_DOMAIN_MAPPER);
    }


    public List<EndUserApplication> findAllEndUserApplicationsByEmployeeId(Select<Record1<Long>> endUserAppIdSelector) {
        return dsl.select(END_USER_APPLICATION.fields())
                .from(END_USER_APPLICATION)
                .where(END_USER_APPLICATION.ID.in(endUserAppIdSelector))
                .fetch(EndUserAppDao.TO_DOMAIN_MAPPER);
    }


    public List<Person> findPeopleByEntityReference(EntityReference ref) {
        return dsl.selectDistinct(PERSON.fields())
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

        Field<String> entityName = InlineSelectFieldFactory.mkNameField(
                    INVOLVEMENT.ENTITY_ID,
                    INVOLVEMENT.ENTITY_KIND,
                    newArrayList(entityKind))
                .as("entity_name");

        return dsl.selectDistinct()
                .select(PERSON.fields())
                .select(INVOLVEMENT.fields())
                .select(entityName)
                .from(PERSON)
                .innerJoin(INVOLVEMENT)
                .on(INVOLVEMENT.EMPLOYEE_ID.eq(PERSON.EMPLOYEE_ID))
                .where(INVOLVEMENT.ENTITY_KIND.eq(entityKind.name())
                        .and(INVOLVEMENT.ENTITY_ID.in(entityIdSelector)
                                .and(INVOLVEMENT.KIND_ID.in(involvementKindIds))))
                .fetch()
                .stream()
                .collect(groupingBy(
                            r -> EntityReference.mkRef(
                                    entityKind,
                                    r.getValue(INVOLVEMENT.ENTITY_ID),
                                    r.getValue(entityName)),
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
}
