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

package org.finos.waltz.data;

import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.jooq.CaseConditionStep;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.*;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.Actor.ACTOR;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static org.finos.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.tables.DataType.DATA_TYPE;
import static org.finos.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static org.finos.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static org.finos.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.jooq.impl.DSL.val;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class InlineSelectFieldFactory {

    // --- Name

    public static Field<String> mkNameField(Field<Long> idCompareField,
                                            Field<String> kindCompareField,
                                            Collection<EntityKind> searchEntityKinds) {
        return NAME_RESOLVER.mkField(idCompareField, kindCompareField, searchEntityKinds);
    }

    public static Field<String> mkNameField(Field<Long> idCompareField,
                                            Field<String> kindCompareField) {
        return NAME_RESOLVER.mkField(idCompareField, kindCompareField, NAME_RESOLVER.getSupportedEntityKinds());
    }


    // --- External Id

    public static Field<String> mkExternalIdField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField,
                                                  Collection<EntityKind> searchEntityKinds) {
        return EXTERNAL_ID_RESOLVER.mkField(idCompareField, kindCompareField, searchEntityKinds);
    }

    public static Field<String> mkExternalIdField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField) {
        return EXTERNAL_ID_RESOLVER.mkField(idCompareField, kindCompareField, EXTERNAL_ID_RESOLVER.getSupportedEntityKinds());
    }


    // --- Lifecycle

    public static Field<String> mkEntityLifecycleField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField,
                                                  Collection<EntityKind> searchEntityKinds) {
        return LIFECYCLE_RESOLVER.mkField(idCompareField, kindCompareField, searchEntityKinds);
    }

    public static Field<String> mkEntityLifecycleField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField) {
        return LIFECYCLE_RESOLVER.mkField(idCompareField, kindCompareField, LIFECYCLE_RESOLVER.getSupportedEntityKinds());
    }


    // --- Internals ----------------------

    private static final InlineSelectFieldFactory NAME_RESOLVER = new InlineSelectFieldFactory(mkNameFieldMappings());
    private static final InlineSelectFieldFactory EXTERNAL_ID_RESOLVER = new InlineSelectFieldFactory(mkExternalIdMappings());
    private static final InlineSelectFieldFactory LIFECYCLE_RESOLVER = new InlineSelectFieldFactory(mkLifecycleFieldMappings());

    private static Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mkNameFieldMappings() {
        Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings = new HashMap<>();
        mappings.put(EntityKind.ACTOR, tuple(ACTOR, ACTOR.ID, ACTOR.NAME));
        mappings.put(EntityKind.ALLOCATION_SCHEME, tuple(ALLOCATION_SCHEME, ALLOCATION_SCHEME.ID, ALLOCATION_SCHEME.NAME));
        mappings.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.NAME));
        mappings.put(EntityKind.APP_GROUP, tuple(APPLICATION_GROUP, APPLICATION_GROUP.ID, APPLICATION_GROUP.NAME));
        mappings.put(EntityKind.CHANGE_INITIATIVE, tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.NAME));
        mappings.put(EntityKind.DATA_TYPE, tuple(DATA_TYPE, DATA_TYPE.ID, DATA_TYPE.NAME));
        mappings.put(EntityKind.FLOW_DIAGRAM, tuple(FLOW_DIAGRAM, FLOW_DIAGRAM.ID, FLOW_DIAGRAM.NAME));
        mappings.put(EntityKind.END_USER_APPLICATION, tuple(END_USER_APPLICATION, END_USER_APPLICATION.ID, END_USER_APPLICATION.NAME));
        mappings.put(EntityKind.ENTITY_STATISTIC, tuple(ENTITY_STATISTIC_DEFINITION, ENTITY_STATISTIC_DEFINITION.ID, ENTITY_STATISTIC_DEFINITION.NAME));
        mappings.put(EntityKind.LEGAL_ENTITY, tuple(LEGAL_ENTITY, LEGAL_ENTITY.ID, LEGAL_ENTITY.NAME));
        mappings.put(EntityKind.LOGICAL_DATA_FLOW, tuple(LOGICAL_FLOW, LOGICAL_FLOW.ID, DSL.val("Logical Flow")));
        mappings.put(EntityKind.MEASURABLE, tuple(MEASURABLE, MEASURABLE.ID, MEASURABLE.NAME));
        mappings.put(EntityKind.MEASURABLE_CATEGORY, tuple(MEASURABLE_CATEGORY, MEASURABLE_CATEGORY.ID, MEASURABLE_CATEGORY.NAME));
        mappings.put(EntityKind.ORG_UNIT, tuple(ORGANISATIONAL_UNIT, ORGANISATIONAL_UNIT.ID, ORGANISATIONAL_UNIT.NAME));
        mappings.put(EntityKind.PERSON, tuple(PERSON, PERSON.ID, PERSON.DISPLAY_NAME));
        mappings.put(EntityKind.PHYSICAL_FLOW, tuple(PHYSICAL_FLOW, PHYSICAL_FLOW.ID, DSL.val("Physical Flow")));
        mappings.put(EntityKind.PHYSICAL_SPECIFICATION, tuple(PHYSICAL_SPECIFICATION, PHYSICAL_SPECIFICATION.ID, PHYSICAL_SPECIFICATION.NAME));
        mappings.put(EntityKind.SERVER, tuple(SERVER_INFORMATION, SERVER_INFORMATION.ID, SERVER_INFORMATION.HOSTNAME));
        mappings.put(EntityKind.LICENCE, tuple(LICENCE, LICENCE.ID, LICENCE.NAME));
        return mappings;
    }

    private static Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mkLifecycleFieldMappings() {
        Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings = new HashMap<>();
        mappings.put(EntityKind.ACTOR, tuple(ACTOR, ACTOR.ID, DSL.val(EntityLifecycleStatus.ACTIVE.name())));
        mappings.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.CHANGE_INITIATIVE, tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, DSL.val(EntityLifecycleStatus.ACTIVE.name())));
        mappings.put(EntityKind.DATA_TYPE, tuple(DATA_TYPE, DATA_TYPE.ID, DSL.val(EntityLifecycleStatus.ACTIVE.name())));
        mappings.put(EntityKind.LEGAL_ENTITY, tuple(LEGAL_ENTITY, LEGAL_ENTITY.ID, LEGAL_ENTITY.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.LOGICAL_DATA_ELEMENT, tuple(LOGICAL_DATA_ELEMENT, LOGICAL_DATA_ELEMENT.ID, LOGICAL_DATA_ELEMENT.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.LOGICAL_DATA_FLOW, tuple(LOGICAL_FLOW, LOGICAL_FLOW.ID, LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.MEASURABLE, tuple(MEASURABLE, MEASURABLE.ID, MEASURABLE.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.PHYSICAL_FLOW, tuple(PHYSICAL_FLOW, PHYSICAL_FLOW.ID, PHYSICAL_FLOW.ENTITY_LIFECYCLE_STATUS));
        return mappings;
    }

    private static Map<EntityKind,Tuple3<Table,Field<Long>,Field<String>>> mkExternalIdMappings() {
        Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings = new HashMap<>();
        mappings.put(EntityKind.ACTOR, tuple(ACTOR, ACTOR.ID, ACTOR.EXTERNAL_ID));
        mappings.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.ASSET_CODE));
        mappings.put(EntityKind.CHANGE_INITIATIVE, tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.EXTERNAL_ID));
        mappings.put(EntityKind.DATA_TYPE, tuple(DATA_TYPE, DATA_TYPE.ID, DATA_TYPE.CODE));
        mappings.put(EntityKind.END_USER_APPLICATION, tuple(END_USER_APPLICATION, END_USER_APPLICATION.ID, END_USER_APPLICATION.EXTERNAL_ID));
        mappings.put(EntityKind.LEGAL_ENTITY, tuple(LEGAL_ENTITY, LEGAL_ENTITY.ID, LEGAL_ENTITY.EXTERNAL_ID));
        mappings.put(EntityKind.MEASURABLE, tuple(MEASURABLE, MEASURABLE.ID, MEASURABLE.EXTERNAL_ID));
        mappings.put(EntityKind.MEASURABLE_CATEGORY, tuple(MEASURABLE_CATEGORY, MEASURABLE_CATEGORY.ID, MEASURABLE_CATEGORY.EXTERNAL_ID));
        mappings.put(EntityKind.PERSON, tuple(PERSON, PERSON.ID, PERSON.EMPLOYEE_ID));
        mappings.put(EntityKind.PHYSICAL_FLOW, tuple(PHYSICAL_FLOW, PHYSICAL_FLOW.ID, PHYSICAL_FLOW.EXTERNAL_ID));
        mappings.put(EntityKind.PHYSICAL_SPECIFICATION, tuple(PHYSICAL_SPECIFICATION, PHYSICAL_SPECIFICATION.ID, PHYSICAL_SPECIFICATION.EXTERNAL_ID));
        mappings.put(EntityKind.SERVER, tuple(SERVER_INFORMATION, SERVER_INFORMATION.ID, SERVER_INFORMATION.EXTERNAL_ID));
        return mappings;
    }

    private final Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings;

    private InlineSelectFieldFactory(Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings) {
        checkNotNull(mappings, "mappings cannot be null");
        this.mappings = mappings;
    }

    /**
     * Creates a derived field to fetch entity names, given fields to compare id and kinds
     * and a list of expected entity kinds.
     *
     * The id and kind comparison fields should refer to the outer table which will be
     * used by the derived field to fetch entity names from appropriate entity tables.
     *
     * Example usage:
     *
     * To make a derived field to fetch entity names for the entity id and kind values
     * stored in {@code Entity_Statistic_Value} table:
     *
     * <code>
     *     <pre>
     *         Field<String> entityNameField = EntityNameUtilities.mkEntityNameField(
     *                                      ENTITY_STATISTIC_VALUE.ENTITY_ID,
     *                                      ENTITY_STATISTIC_VALUE.ENTITY_KIND,
     *                                      newArrayList(EntityKind.APPLICATION, EntityKind.ORG_UNIT));
     *     </pre>
     * <code>
     *
     * then the field can be used in a query as:
     *
     * <code>
     *     <pre>
     *         dsl.select(ENTITY_STATISTIC_VALUE.fields())
     *            .select(entityNameField)
     *            .from(ENTITY_STATISTIC_VALUE);
     *     </pre>
     * </code>
     *
     * the derived field will evaluate to:
     *
     * <code>
     *     <pre>
     *         case
     *          when entity_statistic_value.entity_kind = 'APPLICATION'
     *              then select name from application where id = entity_statistic_value.entity_id
     *          when entity_statistic_value.entity_kind = 'ORG_UNIT'
     *              then select name from organisational_unit where id = entity_statistic_value.entity_id
     *         end
     *     </pre>
     * </code>
     *
     *
     * @param idCompareField field in the outer query that stores entity ids
     * @param kindCompareField field in the outer query that stores entity kinds
     * @param searchEntityKinds list of expected entity kinds in the @kindCompareField
     * @return {@code CASE} field to fetch entity name for each record
     */
    private Field<String> mkField(Field<Long> idCompareField,
                                        Field<String> kindCompareField,
                                        Collection<EntityKind> searchEntityKinds) {
        checkNotNull(idCompareField, "idCompareField cannot be null");
        checkNotNull(kindCompareField, "kindCompareField cannot be null");
        checkNotNull(searchEntityKinds, "searchEntityKinds cannot be null");

        // create case condition and corresponding select statement pairs
        List<Tuple2<Condition, Select<Record1<String>>>> caseSteps = mappings.entrySet().stream()
                .filter(e -> searchEntityKinds.contains(e.getKey()))
                .map(e -> tuple(kindCompareField.eq(val(e.getKey().name())),
                        mkFieldSelect(e.getValue(), idCompareField)))
                .collect(toList());

        // form the where condition field
        // jOOQ doesn't seem to allow creation of case statements
        // through a clean factory method, hence this logic
        CaseConditionStep<String> caseField = null;
        for (Tuple2<Condition, Select<Record1<String>>> caseStep : caseSteps) {
            if (caseField == null) {
                caseField = DSL.when(caseStep.v1(), caseStep.v2());
            } else {
                caseField = caseField.when(caseStep.v1(), caseStep.v2());
            }
        }

        return caseField;
    }


    /**
     * Similar to the three arg version except this one tries all supported entities.
     * As such there is a minor performance penalty - but gives maximum flexibility.
     * @param idCompareField
     * @param kindCompareField
     * @return
     */
    private Field<String> mkField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField) {
        return mkField(idCompareField, kindCompareField, getSupportedEntityKinds());
    }


    private Select<Record1<String>> mkFieldSelect(Tuple3<Table, Field<Long>, Field<String>> mapping,
                                                        Field<Long> idCompareField) {
        // form the query to fetch entity names
        //
        // v1: entity table
        // v3: name field in the entity table
        // v2: id field in the entity table
        //
        // eg: select name from application where id = entity_statistic_value.entity_id
        //
        return DSL.select(mapping.v3())
                .from(mapping.v1())
                .where(mapping.v2().eq(idCompareField));
    }


    private Set<EntityKind> getSupportedEntityKinds() {
        return mappings.keySet();
    }
}
