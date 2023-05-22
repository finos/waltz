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

package org.finos.waltz.data.measurable_category;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.schema.tables.records.MeasurableCategoryRecord;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.measurable_category.ImmutableMeasurableCategory;
import org.finos.waltz.model.measurable_category.MeasurableCategory;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.MeasurableCategory.MEASURABLE_CATEGORY;


@Repository
public class MeasurableCategoryDao {

    public static final RecordMapper<Record, MeasurableCategory> TO_DOMAIN_MAPPER = record -> {
        MeasurableCategoryRecord r = record.into(MEASURABLE_CATEGORY);
        return ImmutableMeasurableCategory.builder()
                .ratingSchemeId(r.getRatingSchemeId())
                .id(r.getId())
                .name(r.getName())
                .externalId(Optional.ofNullable(r.getExternalId()))
                .description(r.getDescription())
                .lastUpdatedBy(r.getLastUpdatedBy())
                .lastUpdatedAt(r.getLastUpdatedAt().toLocalDateTime())
                .editable(r.getEditable())
                .ratingEditorRole(r.getRatingEditorRole())
                .constrainingAssessmentDefinitionId(Optional.ofNullable(r.getConstrainingAssessmentDefinitionId()))
                .icon(r.getIconName())
                .position(r.getPosition())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public MeasurableCategoryDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Collection<MeasurableCategory> findAll() {
        return dsl
                .select(MEASURABLE_CATEGORY.fields())
                .from(MEASURABLE_CATEGORY)
                .orderBy(MEASURABLE_CATEGORY.NAME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public MeasurableCategory getById(long id) {
        return dsl
                .select(MEASURABLE_CATEGORY.fields())
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public Set<MeasurableCategory> findByExternalId(String extId) {
        return dsl
                .select(MEASURABLE_CATEGORY.fields())
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(extId))
                .fetchSet(TO_DOMAIN_MAPPER);
    }

    public Collection<MeasurableCategory> findCategoriesByDirectOrgUnit(long id) {

        SelectConditionStep<Record1<Long>> categoryIds = DSL
                .selectDistinct(MEASURABLE.MEASURABLE_CATEGORY_ID)
                .from(MEASURABLE)
                .innerJoin(ORGANISATIONAL_UNIT).on(MEASURABLE.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .innerJoin(ENTITY_HIERARCHY).on(ORGANISATIONAL_UNIT.ID.eq(ENTITY_HIERARCHY.ID))
                .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(id)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())
                                .and(MEASURABLE.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))));

        return dsl
                .select(MEASURABLE_CATEGORY.fields())
                .from(MEASURABLE_CATEGORY)
                .where(MEASURABLE_CATEGORY.ID.in(categoryIds))
                .fetch(TO_DOMAIN_MAPPER);
    }

    public boolean save(MeasurableCategory measurableCategory, String username) {


        MeasurableCategoryRecord record = dsl.newRecord(MEASURABLE_CATEGORY);
        measurableCategory.id().ifPresent(record::setId);
        record.setName(measurableCategory.name());
        record.setDescription(measurableCategory.description());
        record.setExternalId(measurableCategory.externalId().orElse(null));
        record.setRatingEditorRole(measurableCategory.ratingEditorRole());
        record.setConstrainingAssessmentDefinitionId(measurableCategory.constrainingAssessmentDefinitionId().orElse(null));
        record.setIconName(measurableCategory.icon());
        record.setPosition(measurableCategory.position());
        record.setEditable(measurableCategory.editable());
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy(username);
        record.setRatingSchemeId(measurableCategory.ratingSchemeId());

        record.changed(MEASURABLE_CATEGORY.ID, false);

        int update = record.store();

        return update == 1;
    }
}
