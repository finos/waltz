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

package org.finos.waltz.data.orphan;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.orphan.ImmutableOrphanRelationship;
import org.finos.waltz.model.orphan.OrphanRelationship;
import org.finos.waltz.model.orphan.OrphanSide;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.BiFunction;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static org.finos.waltz.model.EntityLifecycleStatus.REMOVED;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.impl.DSL.select;

@Repository
public class OrphanDao {

    private final DSLContext dsl;

    @Autowired
    public OrphanDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<OrphanRelationship> findApplicationsWithNonExistentOrgUnit() {
        return dsl.select(APPLICATION.ID, APPLICATION.NAME, APPLICATION.ORGANISATIONAL_UNIT_ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID
                        .notIn(select(ORGANISATIONAL_UNIT.ID)
                                .from(ORGANISATIONAL_UNIT)))
                .and(IS_ACTIVE)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.APPLICATION, r.value1(), r.value2()))
                        .entityB(EntityReference.mkRef(EntityKind.ORG_UNIT, r.value3()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanMeasurableRatings() {
        Condition missingCapability = MEASURABLE_RATING.MEASURABLE_ID
                .notIn(select(MEASURABLE.ID)
                        .from(MEASURABLE));

        Condition missingApplication = MEASURABLE_RATING.ENTITY_ID
                .notIn(select(APPLICATION.ID)
                        .from(APPLICATION).where(IS_ACTIVE));

        Condition isApplicationCondition = MEASURABLE_RATING.ENTITY_KIND.eq(EntityKind.APPLICATION.name());

        List<OrphanRelationship> missingMeasurablesForApps = dsl
                .select(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.ENTITY_ID)
                .from(MEASURABLE_RATING)
                .where(missingCapability)
                .and(isApplicationCondition)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.MEASURABLE, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.B)
                        .build());


        List<OrphanRelationship> missingAppsForMeasurables = dsl
                .select(MEASURABLE_RATING.MEASURABLE_ID, MEASURABLE_RATING.ENTITY_ID)
                .from(MEASURABLE_RATING)
                .where(missingApplication)
                .and(isApplicationCondition)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.MEASURABLE, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());

        return ListUtilities.concat(missingAppsForMeasurables, missingMeasurablesForApps);
    }


    public List<OrphanRelationship> findOrphanFlowClassificationRulesByOrgUnit() {
        Condition missingOrgUnit = FLOW_CLASSIFICATION_RULE.PARENT_ID
                .notIn(select(ORGANISATIONAL_UNIT.ID)
                        .from(ORGANISATIONAL_UNIT))
                .and(FLOW_CLASSIFICATION_RULE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()));

       return dsl
               .select(FLOW_CLASSIFICATION_RULE.ID,
                       FLOW_CLASSIFICATION_RULE.PARENT_ID)
                .from(FLOW_CLASSIFICATION_RULE)
                .where(missingOrgUnit)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.FLOW_CLASSIFICATION_RULE, r.value1()))
                        .entityB(mkRef(EntityKind.ORG_UNIT, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());

    }


    public List<OrphanRelationship> findOrphanFlowClassificationRulesByApp() {
        Condition missingApplication = FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID
                .notIn(select(APPLICATION.ID)
                        .from(APPLICATION)
                        .where(IS_ACTIVE))
                .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Condition missingActor = FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID
                .notIn(select(ACTOR.ID)
                        .from(ACTOR))
                .and(FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_KIND.eq(EntityKind.ACTOR.name()));

        return dsl
                .select(FLOW_CLASSIFICATION_RULE.ID,
                        FLOW_CLASSIFICATION_RULE.SUBJECT_ENTITY_ID)
                .from(FLOW_CLASSIFICATION_RULE)
                .where(missingApplication)
                .or(missingActor)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.FLOW_CLASSIFICATION_RULE, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanFlowClassificationRulesByDataType() {
        Condition missingDataType = FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID
                .notIn(select(DATA_TYPE.ID)
                        .from(DATA_TYPE));


        return dsl
                .select(FLOW_CLASSIFICATION_RULE.ID,
                        DATA_TYPE.ID,
                        DATA_TYPE.CODE)
                .from(FLOW_CLASSIFICATION_RULE)
                .leftJoin(DATA_TYPE)
                    .on(FLOW_CLASSIFICATION_RULE.DATA_TYPE_ID.eq(DATA_TYPE.ID))
                .where(missingDataType)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.FLOW_CLASSIFICATION_RULE, r.value1()))
                        .entityB(mkRef(EntityKind.DATA_TYPE, r.value2() != null ? r.value2() : -1, r.value3()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanChangeInitiatives() {
        Condition missingParent = CHANGE_INITIATIVE.PARENT_ID
                .notIn(select(CHANGE_INITIATIVE.ID)
                        .from(CHANGE_INITIATIVE));


        return dsl.select(CHANGE_INITIATIVE.ID,
                CHANGE_INITIATIVE.PARENT_ID)
                .from(CHANGE_INITIATIVE)
                .where(missingParent)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.CHANGE_INITIATIVE, r.value1()))
                        .entityB(mkRef(EntityKind.CHANGE_INITIATIVE, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanLogicalDataFlows() {
        BiFunction<Field<String>, Field<Long>, Select<Record2<Long, Long>>> queryFactory = (kindField, idField) ->
                dsl.select(LOGICAL_FLOW.ID, idField)
                        .from(LOGICAL_FLOW)
                        .where(idField.notIn(
                                select(APPLICATION.ID)
                                        .from(APPLICATION)
                                        .where(IS_ACTIVE)))
                        .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                        .and(kindField.eq(EntityKind.APPLICATION.name()));

        return queryFactory.apply(LOGICAL_FLOW.SOURCE_ENTITY_KIND, LOGICAL_FLOW.SOURCE_ENTITY_ID)
                .unionAll(queryFactory.apply(LOGICAL_FLOW.TARGET_ENTITY_KIND, LOGICAL_FLOW.TARGET_ENTITY_ID))
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.LOGICAL_DATA_FLOW, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());

    }


    public List<OrphanRelationship> findOrphanAttestatations() {

        return dsl
                .selectDistinct(
                        ATTESTATION_INSTANCE.ID,
                        ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .leftJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                            .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNull())
                    .and(APPLICATION.ID.isNull()
                        .or(APPLICATION.ENTITY_LIFECYCLE_STATUS.eq(REMOVED.name()))
                        .or(APPLICATION.IS_REMOVED.eq(true)))
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.ATTESTATION, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanPhysicalFlows() {
        Select<Record1<Long>> allLogicalFlowIds = DSL.select(LOGICAL_FLOW.ID)
                .from(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()));

        Select<Record1<Long>> allPhysicalSpecs = DSL.select(PHYSICAL_SPECIFICATION.ID)
                .from(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.IS_REMOVED.eq(false));

        Condition notRemoved = PHYSICAL_FLOW.IS_REMOVED.eq(false);

        return dsl.select(PHYSICAL_FLOW.ID, PHYSICAL_FLOW.LOGICAL_FLOW_ID, DSL.val(EntityKind.LOGICAL_DATA_FLOW.name()))
                .from(PHYSICAL_FLOW)
                .where(PHYSICAL_FLOW.LOGICAL_FLOW_ID.notIn(allLogicalFlowIds)
                        .and(notRemoved))
                .unionAll(
                        DSL.select(PHYSICAL_FLOW.ID, PHYSICAL_FLOW.ID, DSL.val(EntityKind.PHYSICAL_SPECIFICATION.name()))
                        .from(PHYSICAL_FLOW)
                        .where(PHYSICAL_FLOW.SPECIFICATION_ID.notIn(allPhysicalSpecs)
                                .and(notRemoved))
                )
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.PHYSICAL_FLOW, r.value1()))
                        .entityB(mkRef(EntityKind.valueOf(r.value3()), r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }

}
