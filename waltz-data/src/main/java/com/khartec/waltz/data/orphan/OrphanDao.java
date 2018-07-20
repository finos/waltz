/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.data.orphan;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.orphan.ImmutableOrphanRelationship;
import com.khartec.waltz.model.orphan.OrphanRelationship;
import com.khartec.waltz.model.orphan.OrphanSide;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.function.BiFunction;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.application.ApplicationDao.IS_ACTIVE;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
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

        List<OrphanRelationship> missingMeasurablesForApps = dsl.select(MEASURABLE_RATING.MEASURABLE_ID,
                MEASURABLE_RATING.ENTITY_ID)
                .from(MEASURABLE_RATING)
                .where(missingCapability)
                .and(isApplicationCondition)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.MEASURABLE, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.B)
                        .build());


        List<OrphanRelationship> missingAppsForMeasurables = dsl.select(MEASURABLE_RATING.MEASURABLE_ID,
                MEASURABLE_RATING.ENTITY_ID)
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


    public List<OrphanRelationship> findOrphanAuthoritativeSourceByOrgUnit() {
        Condition missingOrgUnit = AUTHORITATIVE_SOURCE.PARENT_ID
                .notIn(select(ORGANISATIONAL_UNIT.ID)
                        .from(ORGANISATIONAL_UNIT))
                .and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()));

       return dsl.select(AUTHORITATIVE_SOURCE.ID,
                AUTHORITATIVE_SOURCE.PARENT_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(missingOrgUnit)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.AUTHORITATIVE_SOURCE, r.value1()))
                        .entityB(mkRef(EntityKind.ORG_UNIT, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());

    }


    public List<OrphanRelationship> findOrphanAuthoritativeSourceByApp() {
        Condition missingApplication = AUTHORITATIVE_SOURCE.APPLICATION_ID
                .notIn(select(APPLICATION.ID)
                        .from(APPLICATION)
                        .where(IS_ACTIVE));


        return dsl.select(AUTHORITATIVE_SOURCE.ID,
                AUTHORITATIVE_SOURCE.APPLICATION_ID)
                .from(AUTHORITATIVE_SOURCE)
                .where(missingApplication)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.AUTHORITATIVE_SOURCE, r.value1()))
                        .entityB(mkRef(EntityKind.APPLICATION, r.value2()))
                        .orphanSide(OrphanSide.A)
                        .build());
    }


    public List<OrphanRelationship> findOrphanAuthoritiveSourceByDataType() {
        Condition missingDataType = AUTHORITATIVE_SOURCE.DATA_TYPE
                .notIn(select(DATA_TYPE.CODE)
                        .from(DATA_TYPE));


        return dsl.select(AUTHORITATIVE_SOURCE.ID,
                DATA_TYPE.ID,
                AUTHORITATIVE_SOURCE.DATA_TYPE)
                .from(AUTHORITATIVE_SOURCE)
                .leftJoin(DATA_TYPE)
                    .on(AUTHORITATIVE_SOURCE.DATA_TYPE.eq(DATA_TYPE.CODE))
                .where(missingDataType)
                .fetch(r -> ImmutableOrphanRelationship.builder()
                        .entityA(mkRef(EntityKind.AUTHORITATIVE_SOURCE, r.value1()))
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
                DSL.select(LOGICAL_FLOW.ID, idField)
                        .from(LOGICAL_FLOW)
                        .where(idField.notIn(
                                select(APPLICATION.ID)
                                        .from(APPLICATION)
                                        .where(IS_ACTIVE)))
                        .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name()))
                        .and(kindField.eq(EntityKind.APPLICATION.name()));

        return dsl.selectFrom(queryFactory.apply(LOGICAL_FLOW.SOURCE_ENTITY_KIND, LOGICAL_FLOW.SOURCE_ENTITY_ID).asTable())
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
