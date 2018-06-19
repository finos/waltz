/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.stream.Collectors.toList;
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
        mappings.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.NAME));
        mappings.put(EntityKind.APP_GROUP, tuple(APPLICATION_GROUP, APPLICATION_GROUP.ID, APPLICATION_GROUP.NAME));
        mappings.put(EntityKind.CHANGE_INITIATIVE, tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.NAME));
        mappings.put(EntityKind.DATA_TYPE, tuple(DATA_TYPE, DATA_TYPE.ID, DATA_TYPE.NAME));
        mappings.put(EntityKind.FLOW_DIAGRAM, tuple(FLOW_DIAGRAM, FLOW_DIAGRAM.ID, FLOW_DIAGRAM.NAME));
        mappings.put(EntityKind.END_USER_APPLICATION, tuple(END_USER_APPLICATION, END_USER_APPLICATION.ID, END_USER_APPLICATION.NAME));
        mappings.put(EntityKind.ENTITY_STATISTIC, tuple(ENTITY_STATISTIC_DEFINITION, ENTITY_STATISTIC_DEFINITION.ID, ENTITY_STATISTIC_DEFINITION.NAME));
        mappings.put(EntityKind.MEASURABLE, tuple(MEASURABLE, MEASURABLE.ID, MEASURABLE.NAME));
        mappings.put(EntityKind.MEASURABLE_CATEGORY, tuple(MEASURABLE_CATEGORY, MEASURABLE_CATEGORY.ID, MEASURABLE_CATEGORY.NAME));
        mappings.put(EntityKind.ORG_UNIT, tuple(ORGANISATIONAL_UNIT, ORGANISATIONAL_UNIT.ID, ORGANISATIONAL_UNIT.NAME));
        mappings.put(EntityKind.PERSON, tuple(PERSON, PERSON.ID, PERSON.DISPLAY_NAME));
        mappings.put(EntityKind.PHYSICAL_SPECIFICATION, tuple(PHYSICAL_SPECIFICATION, PHYSICAL_SPECIFICATION.ID, PHYSICAL_SPECIFICATION.NAME));
        return mappings;
    }

    private static Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mkLifecycleFieldMappings() {
        Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings = new HashMap<>();
        mappings.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.LOGICAL_DATA_ELEMENT, tuple(LOGICAL_DATA_ELEMENT, LOGICAL_DATA_ELEMENT.ID, LOGICAL_DATA_ELEMENT.ENTITY_LIFECYCLE_STATUS));
        mappings.put(EntityKind.ACTOR, tuple(ACTOR, ACTOR.ID, DSL.val(EntityLifecycleStatus.ACTIVE.name())));
        return mappings;
    }

    private static Map<EntityKind,Tuple3<Table,Field<Long>,Field<String>>> mkExternalIdMappings() {
        Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> mappings = new HashMap<>();
        mappings.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.ASSET_CODE));
        mappings.put(EntityKind.CHANGE_INITIATIVE, tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.EXTERNAL_ID));
        mappings.put(EntityKind.DATA_TYPE, tuple(DATA_TYPE, DATA_TYPE.ID, DATA_TYPE.CODE));
        mappings.put(EntityKind.END_USER_APPLICATION, tuple(END_USER_APPLICATION, END_USER_APPLICATION.ID, END_USER_APPLICATION.EXTERNAL_ID));
        mappings.put(EntityKind.MEASURABLE, tuple(MEASURABLE, MEASURABLE.ID, MEASURABLE.EXTERNAL_ID));
        mappings.put(EntityKind.MEASURABLE_CATEGORY, tuple(MEASURABLE_CATEGORY, MEASURABLE_CATEGORY.ID, MEASURABLE_CATEGORY.EXTERNAL_ID));
        mappings.put(EntityKind.PERSON, tuple(PERSON, PERSON.ID, PERSON.EMPLOYEE_ID));
        mappings.put(EntityKind.PHYSICAL_SPECIFICATION, tuple(PHYSICAL_SPECIFICATION, PHYSICAL_SPECIFICATION.ID, PHYSICAL_SPECIFICATION.EXTERNAL_ID));
        mappings.put(EntityKind.PHYSICAL_FLOW, tuple(PHYSICAL_FLOW, PHYSICAL_FLOW.ID, PHYSICAL_FLOW.EXTERNAL_ID));
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
