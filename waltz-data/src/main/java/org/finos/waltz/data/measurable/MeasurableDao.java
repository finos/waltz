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

package org.finos.waltz.data.measurable;


import org.finos.waltz.schema.tables.records.MeasurableRecord;
import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.FindEntityReferencesByIdSelector;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.measurable.ImmutableMeasurable;
import org.finos.waltz.model.measurable.Measurable;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static org.finos.waltz.schema.tables.Measurable.MEASURABLE;
import static java.util.Optional.ofNullable;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.EnumUtilities.readEnum;
import static org.finos.waltz.common.StringUtilities.mkSafe;
import static org.finos.waltz.data.JooqUtilities.TO_ENTITY_REFERENCE;


@Repository
public class MeasurableDao implements FindEntityReferencesByIdSelector {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableDao.class);

    public static RecordMapper<Record, Measurable> TO_DOMAIN_MAPPER = record -> {
        MeasurableRecord r = record.into(MEASURABLE);

        return ImmutableMeasurable.builder()
                .id(r.getId())
                .parentId(ofNullable(r.getParentId()))
                .name(r.getName())
                .categoryId(r.getMeasurableCategoryId())
                .concrete(r.getConcrete())
                .description(r.getDescription())
                .externalId(ofNullable(r.getExternalId()))
                .externalParentId(ofNullable(r.getExternalParentId()))
                .provenance(r.getProvenance())
                .lastUpdatedAt(DateTimeUtilities.toLocalDateTime(r.getLastUpdatedAt()))
                .lastUpdatedBy(r.getLastUpdatedBy())
                .entityLifecycleStatus(readEnum(r.getEntityLifecycleStatus(), EntityLifecycleStatus.class, s -> EntityLifecycleStatus.ACTIVE))
                .organisationalUnitId(r.getOrganisationalUnitId())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public MeasurableDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<Measurable> findAll() {
        return dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    @Override
    public List<EntityReference> findByIdSelectorAsEntityReference(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        return dsl
                .select(MEASURABLE.ID, MEASURABLE.NAME, DSL.val(EntityKind.MEASURABLE.name()))
                .from(MEASURABLE)
                .where(MEASURABLE.ID.in(selector))
                .fetch(TO_ENTITY_REFERENCE);
    }


    public List<Measurable> findByMeasurableIdSelector(Select<Record1<Long>> selector) {
        checkNotNull(selector, "selector cannot be null");
        SelectConditionStep<Record> qry = dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(dsl.renderInlined(MEASURABLE.ID.in(selector)));

        System.out.println(qry);
        return qry.fetch(TO_DOMAIN_MAPPER);
    }


    public Measurable getById(long id) {
        return dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(MEASURABLE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public Collection<Measurable> findByExternalId(String extId) {
        return dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(MEASURABLE.EXTERNAL_ID.eq(extId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean updateConcreteFlag(Long id, boolean newValue, String userId) {
        return updateField(id, MEASURABLE.CONCRETE, newValue, userId);
    }


    public boolean updateName(long id, String newValue, String userId) {
        return updateField(id, MEASURABLE.NAME, newValue, userId);
    }


    public boolean updateDescription(long id, String newValue, String userId) {
        return updateField(id, MEASURABLE.DESCRIPTION, newValue, userId);
    }


    public boolean updateExternalId(long id, String newValue, String userId) {
        return updateField(id, MEASURABLE.EXTERNAL_ID, newValue, userId);
    }


    private <T> boolean updateField(long id, Field<T> field, T value, String userId) {
        return dsl
                .update(MEASURABLE)
                .set(field, value)
                .set(MEASURABLE.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(MEASURABLE.LAST_UPDATED_BY, userId)
                .where(MEASURABLE.ID.eq(id))
                .execute() == 1;
    }


    public Long create(Measurable measurable) {
        return dsl.insertInto(MEASURABLE)
                .set(MEASURABLE.MEASURABLE_CATEGORY_ID, measurable.categoryId())
                .set(MEASURABLE.PARENT_ID, measurable.parentId().orElse(null))
                .set(MEASURABLE.EXTERNAL_ID, measurable.externalId().orElse(null))
                .set(MEASURABLE.EXTERNAL_PARENT_ID, measurable.externalParentId().orElse(null))
                .set(MEASURABLE.NAME, measurable.name())
                .set(MEASURABLE.CONCRETE, measurable.concrete())
                .set(MEASURABLE.DESCRIPTION, mkSafe(measurable.description()))
                .set(MEASURABLE.PROVENANCE, "waltz")
                .set(MEASURABLE.LAST_UPDATED_BY, measurable.lastUpdatedBy())
                .set(MEASURABLE.LAST_UPDATED_AT, Timestamp.valueOf(measurable.lastUpdatedAt()))
                .returning(MEASURABLE.ID)
                .fetchOne()
                .getId();
    }


    /**
     * Bulk removes measurables by using the passed in selector.
     * Removed measurables are _deleted_ from the database.
     *
     * @param selector gives id's of measurables to remove
     * @return count of removed measurables
     */
    public int deleteByIdSelector(Select<Record1<Long>> selector) {
        return dsl
                .update(MEASURABLE)
                .set(MEASURABLE.ENTITY_LIFECYCLE_STATUS, EntityLifecycleStatus.REMOVED.name())
                .where(MEASURABLE.ID.in(selector))
                .execute();
    }


    public boolean updateParentId(Long measurableId, Long destinationId, String userId) {
        LOG.info(
                "Moving measurable: {} to {}",
                measurableId,
                destinationId == null
                        ? "root of category"
                        : destinationId);

        Select<? extends Record1<String>> destinationExtId = destinationId == null
                ? null
                : DSL
                    .select(MEASURABLE.EXTERNAL_ID)
                    .from(MEASURABLE)
                    .where(MEASURABLE.ID.eq(destinationId));

        return dsl
                .update(MEASURABLE)
                .set(MEASURABLE.PARENT_ID, destinationId)
                .set(MEASURABLE.EXTERNAL_PARENT_ID, destinationExtId)
                .set(MEASURABLE.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(MEASURABLE.LAST_UPDATED_BY, userId)
                .where(MEASURABLE.ID.eq(measurableId))
                .execute() == 1;
    }


    public List<Measurable> findByCategoryId(Long categoryId) {
        return dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .and(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Map<String, Long> findExternalIdToIdMapByCategoryId(Long categoryId) {
        return dsl
                .select(MEASURABLE.EXTERNAL_ID, MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .and(MEASURABLE.EXTERNAL_ID.isNotNull())
                .fetchMap(MEASURABLE.EXTERNAL_ID, MEASURABLE.ID);
    }

    /**
     * Allows measurable rating, planned decoms and replacement apps to check for the role
     * @param reference Entity ref to determine condition
     * @return 'rating_editor_role'
     */
    public String getRequiredRatingEditRole(EntityReference reference) {

        Condition condition = mkCondition(reference);

        Condition measurableToRatingJoinCondition = MEASURABLE_RATING.MEASURABLE_ID.eq(MEASURABLE_RATING_PLANNED_DECOMMISSION.MEASURABLE_ID)
                .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_ID.eq(MEASURABLE_RATING.ENTITY_ID)
                        .and(MEASURABLE_RATING_PLANNED_DECOMMISSION.ENTITY_KIND.eq(MEASURABLE_RATING.ENTITY_KIND)));

        return dsl
                .selectDistinct(MEASURABLE_CATEGORY.RATING_EDITOR_ROLE)
                .from(MEASURABLE_CATEGORY)
                .innerJoin(MEASURABLE).on(MEASURABLE_CATEGORY.ID.eq(MEASURABLE.MEASURABLE_CATEGORY_ID))
                .leftJoin(MEASURABLE_RATING).on(MEASURABLE_RATING.MEASURABLE_ID.eq(MEASURABLE.ID))
                .leftJoin(MEASURABLE_RATING_PLANNED_DECOMMISSION).on(measurableToRatingJoinCondition)
                .where(condition)
                .fetchOne()
                .get(MEASURABLE_CATEGORY.RATING_EDITOR_ROLE);
    }


    private Condition mkCondition(EntityReference reference) {
        if (reference.kind().equals(EntityKind.MEASURABLE)){
            return MEASURABLE.ID.eq(reference.id());
        } else if (reference.kind().equals(EntityKind.MEASURABLE_CATEGORY)){
            return MEASURABLE_CATEGORY.ID.eq(reference.id());
        } else {
            return MEASURABLE_RATING_PLANNED_DECOMMISSION.ID.eq(reference.id());
        }
    }


    public Collection<Measurable> findByOrgUnitId(Long orgUnitId) {

        SelectConditionStep<Record1<Long>> orgUnitOrChildIds = DSL
                .select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.ANCESTOR_ID.in(orgUnitId)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())));

        return dsl
                .select(MEASURABLE.fields())
                .from(MEASURABLE)
                .where(MEASURABLE.ORGANISATIONAL_UNIT_ID.in(orgUnitOrChildIds))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
