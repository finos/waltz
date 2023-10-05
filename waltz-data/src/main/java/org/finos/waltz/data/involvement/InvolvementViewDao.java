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

import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.model.CommonTableFields;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyDirection;
import org.finos.waltz.model.involvement.ImmutableInvolvement;
import org.finos.waltz.model.involvement.ImmutableInvolvementDetail;
import org.finos.waltz.model.involvement.InvolvementDetail;
import org.finos.waltz.model.involvement_kind.ImmutableInvolvementKind;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.Involvement;
import org.finos.waltz.schema.tables.InvolvementKind;
import org.finos.waltz.schema.tables.Person;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.finos.waltz.common.ListUtilities.append;
import static org.finos.waltz.common.ListUtilities.asList;
import static org.finos.waltz.common.ListUtilities.concat;
import static org.finos.waltz.data.JooqUtilities.determineCommonTableFields;
import static org.finos.waltz.model.EntityReference.mkRef;


@Repository
public class InvolvementViewDao {

    private static final InvolvementKind ik = Tables.INVOLVEMENT_KIND;
    private static final Involvement i = Tables.INVOLVEMENT;
    private static final EntityHierarchy eh = Tables.ENTITY_HIERARCHY;
    private static final Person p = Tables.PERSON;

    private final DSLContext dsl;

    @Autowired
    public InvolvementViewDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Map<HierarchyDirection, ? extends Collection<InvolvementDetail>> findAllInvolvements(EntityReference ref) {
        CommonTableFields<?> t = determineCommonTableFields(ref.kind());

        return mkInvolvementApiQuery(dsl, ref)
            .fetchGroups(
                    r -> HierarchyDirection.valueOf(r.get("direction", String.class)),
                    r -> ImmutableInvolvementDetail
                        .builder()
                        .involvement(ImmutableInvolvement
                                .builder()
                                .kindId(r.get(ik.ID))
                                .employeeId(r.get(p.EMPLOYEE_ID))
                                .entityReference(mkRef(
                                        t.entityKind(),
                                        r.get(t.idField()),
                                        r.get(t.nameField())))
                                .isReadOnly(r.get(i.IS_READONLY))
                                .build())
                        .involvementKind(ImmutableInvolvementKind
                                .builder()
                                .subjectKind(t.entityKind())
                                .name(r.get(ik.NAME))
                                .id(r.get(ik.ID))
                                .lastUpdatedBy("n/a")
                                .build())
                        .person(PersonDao.personMapper.map(r))
                        .build());

    }


    public static Select<Record> mkInvolvementApiQuery(DSLContext dsl,
                                                       EntityReference ref) {

        CommonTableFields<?> t = determineCommonTableFields(ref.kind());

        List<Field<?>> commonQueryFields = concat(
                asList(
                    t.idField(),
                    t.nameField(),
                    ik.NAME,
                    ik.ID,
                    i.IS_READONLY),
                asList(p.fields()));

        return mkInvolvementQuery(
                dsl,
                ref,
                commonQueryFields);
    }


    /**
     * Generates a query suitable by use of an extractor. If is differentiated from the api query by:
     *
     * <ul>
     *     <li>Has less fields, only the ones needed for the csv/xlsx output</li>
     *     <li>Uses 'pretty' names for the fields</li>
     * </ul>
     *
     * @param dsl  connection to use
     * @param ref  entity reference to use
     * @return  jOOQ query which can have data 'fetched' against it
     */
    public static Select<Record> mkInvolvementExtractorQuery(DSLContext dsl,
                                                             EntityReference ref) {

        CommonTableFields<?> t = determineCommonTableFields(ref.kind());

        List<Field<?>> commonQueryFields = asList(
                p.DISPLAY_NAME.as("Person Name"),
                p.TITLE.as("Person Title"),
                p.OFFICE_PHONE.as("Telephone"),
                p.EMAIL.as("Email"),
                p.IS_REMOVED.as("Is Removed"),
                ik.NAME.as("Involvement"),
                t.nameField().as(format("%s Name", t.entityKind().prettyName())),
                t.externalIdField().as(format("%s External Id", t.entityKind().prettyName())));

        return mkInvolvementQuery(
                dsl,
                ref,
                commonQueryFields);
    }


    private static Select<Record> mkInvolvementQuery(DSLContext dsl,
                                                     EntityReference ref,
                                                     List<Field<?>> commonQueryFields) {
        CommonTableFields<?> t = determineCommonTableFields(ref.kind());

        List<Field<?>> descendentFields = append(
                commonQueryFields,
                DSL.when(t.idField().eq(ref.id()), "EXACT")
                        .otherwise("DESCENDENT")
                        .as("direction"));

        List<Field<?>> ancestorFields = append(
                commonQueryFields,
                DSL.when(t.idField().eq(ref.id()), "EXACT")
                        .otherwise("ANCESTOR")
                        .as("direction"));

        List<Field<?>> exactFields = append(
                commonQueryFields,
                DSL.value("EXACT").as("direction"));

        if (t.hierarchical()) {
            Select<Record> descendents = dsl
                    .select(descendentFields)
                    .from(t.table())
                    .innerJoin(eh)
                    .on(eh.ID.eq(t.idField())
                            .and(eh.KIND.eq(t.entityKind().name()))
                            .and(eh.ANCESTOR_ID.eq(ref.id())))
                    .innerJoin(i)
                    .on(t.idField().eq(i.ENTITY_ID)
                            .and(i.ENTITY_KIND.eq(t.entityKind().name())))
                    .innerJoin(ik)
                    .on(i.KIND_ID.eq(ik.ID))
                    .innerJoin(p)
                    .on(i.EMPLOYEE_ID.eq(p.EMPLOYEE_ID))
                    .where(t.isActiveCondition());

            Select<Record> ancestors = dsl
                    .select(ancestorFields)
                    .from(t.table())
                    .innerJoin(eh)
                    .on(eh.ANCESTOR_ID.eq(t.idField())
                            .and(eh.KIND.eq(t.entityKind().name()))
                            .and(eh.ID.eq(ref.id())))
                    .innerJoin(i)
                    .on(t.idField().eq(i.ENTITY_ID)
                            .and(i.ENTITY_KIND.eq(t.entityKind().name())))
                    .innerJoin(ik)
                    .on(i.KIND_ID.eq(ik.ID))
                    .innerJoin(p)
                    .on(i.EMPLOYEE_ID.eq(p.EMPLOYEE_ID))
                    .where(t.isActiveCondition());

            return descendents
                    .union(ancestors);

        } else {
            return  dsl
                    .select(exactFields)
                    .from(t.table())
                    .innerJoin(i)
                    .on(t.idField().eq(i.ENTITY_ID)
                            .and(i.ENTITY_KIND.eq(t.entityKind().name())))
                    .innerJoin(ik)
                    .on(i.KIND_ID.eq(ik.ID))
                    .innerJoin(p)
                    .on(i.EMPLOYEE_ID.eq(p.EMPLOYEE_ID))
                    .where(t.idField().eq(ref.id())
                            .and(t.isActiveCondition()));
        }
    }


}
