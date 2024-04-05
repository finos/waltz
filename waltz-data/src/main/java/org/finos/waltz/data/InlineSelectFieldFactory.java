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

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.model.CommonTableFields;
import org.finos.waltz.model.EntityKind;
import org.jooq.CaseConditionStep;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.jooq.impl.DSL.val;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class InlineSelectFieldFactory {

    private static final Set<EntityKind> DEFAULT_LIFECYCLE_ENTITIES = SetUtilities.asSet(
            EntityKind.ACTOR,
            EntityKind.APPLICATION,
            EntityKind.CHANGE_INITIATIVE,
            EntityKind.DATA_TYPE,
            EntityKind.LEGAL_ENTITY,
            EntityKind.LOGICAL_DATA_ELEMENT,
            EntityKind.LOGICAL_DATA_FLOW,
            EntityKind.MEASURABLE,
            EntityKind.PHYSICAL_FLOW);

    private static final Set<EntityKind> DEFAULT_EXTERNAL_ID_ENTITIES = SetUtilities.asSet(
            EntityKind.ACTOR,
            EntityKind.APPLICATION,
            EntityKind.CHANGE_INITIATIVE,
            EntityKind.CHANGE_SET,
            EntityKind.DATA_TYPE,
            EntityKind.END_USER_APPLICATION,
            EntityKind.LOGICAL_DATA_FLOW,
            EntityKind.LEGAL_ENTITY,
            EntityKind.MEASURABLE,
            EntityKind.MEASURABLE_CATEGORY,
            EntityKind.PERSON,
            EntityKind.PHYSICAL_FLOW,
            EntityKind.PHYSICAL_SPECIFICATION,
            EntityKind.SERVER,
            EntityKind.SOFTWARE);

    private static final Set<EntityKind> DEFAULT_NAME_ENTITIES = SetUtilities.asSet(
            EntityKind.ACTOR,
            EntityKind.ALLOCATION_SCHEME,
            EntityKind.APPLICATION,
            EntityKind.APP_GROUP,
            EntityKind.CHANGE_INITIATIVE,
            EntityKind.CHANGE_SET,
            EntityKind.DATA_TYPE,
            EntityKind.END_USER_APPLICATION,
            EntityKind.ENTITY_RELATIONSHIP,
            EntityKind.ENTITY_STATISTIC,
            EntityKind.FLOW_CLASSIFICATION_RULE,
            EntityKind.FLOW_DIAGRAM,
            EntityKind.INVOLVEMENT_KIND,
            EntityKind.LEGAL_ENTITY,
            EntityKind.LEGAL_ENTITY_RELATIONSHIP,
            EntityKind.LICENCE,
            EntityKind.LOGICAL_DATA_FLOW,
            EntityKind.MEASURABLE,
            EntityKind.MEASURABLE_RATING,
            EntityKind.MEASURABLE_CATEGORY,
            EntityKind.ORG_UNIT,
            EntityKind.PERSON,
            EntityKind.PHYSICAL_FLOW,
            EntityKind.PHYSICAL_SPECIFICATION,
            EntityKind.SERVER,
            EntityKind.SOFTWARE);

    // --- Name

    public static Field<String> mkNameField(Field<Long> idCompareField,
                                            Field<String> kindCompareField,
                                            Collection<EntityKind> searchEntityKinds) {
        return mkField(
                idCompareField,
                kindCompareField,
                searchEntityKinds,
                CommonTableFields::nameField);
    }

    public static Field<String> mkNameField(Field<Long> idCompareField,
                                            Field<String> kindCompareField) {
        return mkNameField(
                idCompareField,
                kindCompareField,
                DEFAULT_NAME_ENTITIES);
    }


    // --- External Id

    public static Field<String> mkExternalIdField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField,
                                                  Collection<EntityKind> searchEntityKinds) {
        return mkField(
                idCompareField,
                kindCompareField,
                searchEntityKinds,
                CommonTableFields::externalIdField);
    }

    public static Field<String> mkExternalIdField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField) {
        return mkExternalIdField(
                idCompareField,
                kindCompareField,
                DEFAULT_EXTERNAL_ID_ENTITIES);
    }


    // --- Lifecycle

    public static Field<String> mkEntityLifecycleField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField,
                                                  Collection<EntityKind> searchEntityKinds) {
        return mkField(
                idCompareField,
                kindCompareField,
                searchEntityKinds,
                CommonTableFields::lifecycleField);
    }

    public static Field<String> mkEntityLifecycleField(Field<Long> idCompareField,
                                                  Field<String> kindCompareField) {
        return mkEntityLifecycleField(
                idCompareField,
                kindCompareField,
                DEFAULT_LIFECYCLE_ENTITIES);
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
    private static Field<String> mkField(Field<Long> idCompareField,
                                         Field<String> kindCompareField,
                                         Collection<EntityKind> searchEntityKinds,
                                         Function<CommonTableFields<?>, Field<String>> targetFieldResolver) {
        checkNotNull(idCompareField, "idCompareField cannot be null");
        checkNotNull(kindCompareField, "kindCompareField cannot be null");
        checkNotNull(searchEntityKinds, "searchEntityKinds cannot be null");

        List<Tuple2<Condition, SelectConditionStep<Record1<String>>>> caseSteps = searchEntityKinds
                .stream()
                .map(JooqUtilities::determineCommonTableFields)
                .map(ctf -> tuple(
                        kindCompareField.eq(val(ctf.entityKind().name())),
                        DSL.select(Optional
                                        .ofNullable(targetFieldResolver.apply(ctf))
                                        .orElse(CommonTableFields.NA_FIELD_VAL))
                                .from(ctf.table())
                                .where(ctf.idField().eq(idCompareField))))
                .collect(toList());

        // form the where condition field
        // jOOQ doesn't seem to allow creation of case statements
        // through a clean factory method, hence this logic
        CaseConditionStep<String> caseField = null;
        for (Tuple2<Condition, SelectConditionStep<Record1<String>>> caseStep : caseSteps) {
            if (caseField == null) {
                caseField = DSL.when(caseStep.v1(), caseStep.v2());
            } else {
                caseField = caseField.when(caseStep.v1(), caseStep.v2());
            }
        }

        return caseField;
    }

}
