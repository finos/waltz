/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data;

import com.khartec.waltz.model.EntityKind;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;

import java.util.*;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.FLOW_DIAGRAM;
import static com.khartec.waltz.schema.Tables.MEASURABLE;
import static com.khartec.waltz.schema.tables.Actor.ACTOR;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.val;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class EntityNameUtilities {

    /**
     * Stores the table and fields required to fetch name for each entity kind
     * Tuple3:
     * v1: Entity Table
     * v2: ID field in the Entity table
     * v3: Name field in the Entity table
     */
    private static final Map<EntityKind, Tuple3<Table, Field<Long>, Field<String>>> MAPPINGS;


    static {
        MAPPINGS = new HashMap<>(EntityKind.values().length);
        MAPPINGS.put(EntityKind.ACTOR, tuple(ACTOR, ACTOR.ID, ACTOR.NAME));
        MAPPINGS.put(EntityKind.APPLICATION, tuple(APPLICATION, APPLICATION.ID, APPLICATION.NAME));
        MAPPINGS.put(EntityKind.APP_GROUP, tuple(APPLICATION_GROUP, APPLICATION_GROUP.ID, APPLICATION_GROUP.NAME));
        MAPPINGS.put(EntityKind.CHANGE_INITIATIVE, tuple(CHANGE_INITIATIVE, CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.NAME));
        MAPPINGS.put(EntityKind.DATA_TYPE, tuple(DATA_TYPE, DATA_TYPE.ID, DATA_TYPE.NAME));
        MAPPINGS.put(EntityKind.FLOW_DIAGRAM, tuple(FLOW_DIAGRAM, FLOW_DIAGRAM.ID, FLOW_DIAGRAM.NAME));
        MAPPINGS.put(EntityKind.END_USER_APPLICATION, tuple(END_USER_APPLICATION, END_USER_APPLICATION.ID, END_USER_APPLICATION.NAME));
        MAPPINGS.put(EntityKind.ENTITY_STATISTIC, tuple(ENTITY_STATISTIC_DEFINITION, ENTITY_STATISTIC_DEFINITION.ID, ENTITY_STATISTIC_DEFINITION.NAME));
        MAPPINGS.put(EntityKind.MEASURABLE, tuple(MEASURABLE, MEASURABLE.ID, MEASURABLE.NAME));
        MAPPINGS.put(EntityKind.ORG_UNIT, tuple(ORGANISATIONAL_UNIT, ORGANISATIONAL_UNIT.ID, ORGANISATIONAL_UNIT.NAME));
        MAPPINGS.put(EntityKind.PERSON, tuple(PERSON, PERSON.ID, PERSON.DISPLAY_NAME));
        MAPPINGS.put(EntityKind.PHYSICAL_SPECIFICATION, tuple(PHYSICAL_SPECIFICATION, PHYSICAL_SPECIFICATION.ID, PHYSICAL_SPECIFICATION.NAME));
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
    public static Field<String> mkEntityNameField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField,
                                                  Collection<EntityKind> searchEntityKinds) {
        checkNotNull(idCompareField, "idCompareField cannot be null");
        checkNotNull(kindCompareField, "kindCompareField cannot be null");
        checkNotNull(searchEntityKinds, "searchEntityKinds cannot be null");

        // create case condition and corresponding select statement pairs
        List<Tuple2<Condition, Select<Record1<String>>>> caseSteps = MAPPINGS.entrySet().stream()
                .filter(e -> searchEntityKinds.contains(e.getKey()))
                .map(e -> tuple(kindCompareField.eq(val(e.getKey().name())),
                                mkNameSelect(e.getValue(), idCompareField)))
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
    public static Field<String> mkEntityNameField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField) {
        return mkEntityNameField(idCompareField, kindCompareField, getSupportedEntityKinds());
    }


    private static Select<Record1<String>> mkNameSelect(Tuple3<Table, Field<Long>, Field<String>> mapping,
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


    private static Set<EntityKind> getSupportedEntityKinds() {
        return MAPPINGS.keySet();
    }
}
