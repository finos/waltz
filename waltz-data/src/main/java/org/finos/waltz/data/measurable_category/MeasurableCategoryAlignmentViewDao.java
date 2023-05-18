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

import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.measurable.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.groupAndThen;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.jooq.impl.DSL.name;


@Repository
public class MeasurableCategoryAlignmentViewDao {

    private static final MeasurableRating mr = Tables.MEASURABLE_RATING.as("mr");
    private static final org.finos.waltz.schema.tables.Measurable m = Tables.MEASURABLE.as("m");
    private static final MeasurableCategory mc = Tables.MEASURABLE_CATEGORY.as("mc");
    private static final EntityHierarchy eh = Tables.ENTITY_HIERARCHY.as("eh");

    private final DSLContext dsl;

    @Autowired
    public MeasurableCategoryAlignmentViewDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Set<MeasurableCategoryAlignment> findAlignmentsByAppSelector(Select<Record1<Long>> selector){

        CommonTableExpression<Record3<Long, Long, String>> ratings = name("ratings")
                .as(DSL
                        .select(mr.MEASURABLE_ID, mr.ENTITY_ID, mr.RATING.as("rating"))
                        .from(mr)
                        .where(mr.ENTITY_ID.in(selector)
                                .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))));


        CommonTableExpression<Record3<Long, Long, String>> parents_with_ratings = name("parents_with_ratings")
                .as(DSL
                        .selectDistinct(eh.ANCESTOR_ID, ratings.field(mr.ENTITY_ID), ratings.field(mr.RATING))
                        .from(ratings)
                        .innerJoin(eh).on(ratings.field(mr.MEASURABLE_ID).eq(eh.ID))
                        .where(eh.KIND.eq(EntityKind.MEASURABLE.name())));


        Map<EntityReference, Result<Record11<String, Long, Long, String, Long, Boolean, String, Long, Integer, Long, String>>> alignmentsByMeasurableCategoryRef = dsl
                .with(ratings)
                .with(parents_with_ratings)
                .select(mc.NAME,
                        mc.ID,
                        m.ID,
                        m.NAME,
                        m.PARENT_ID,
                        m.CONCRETE,
                        m.LAST_UPDATED_BY,
                        m.ORGANISATIONAL_UNIT_ID,
                        m.POSITION,
                        parents_with_ratings.field(mr.ENTITY_ID),
                        parents_with_ratings.field(mr.RATING))
                .from(parents_with_ratings)
                .innerJoin(m)
                .on(parents_with_ratings.field(eh.ANCESTOR_ID).eq(m.ID))
                .innerJoin(mc).on(m.MEASURABLE_CATEGORY_ID.eq(mc.ID))
                .where(m.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetchGroups(r -> mkRef(EntityKind.MEASURABLE_CATEGORY, r.get(mc.ID), r.get(mc.NAME)));


        Set<MeasurableCategoryAlignment> categoryAlignments = alignmentsByMeasurableCategoryRef
                .entrySet()
                .stream()
                .map(e -> {
                    Result<Record11<String, Long, Long, String, Long, Boolean, String, Long, Integer, Long, String>> values = e.getValue();
                    EntityReference categoryRef = e.getKey();

                    Map<Measurable, Set<Long>> measurableAlignments = groupAndThen(
                            values,
                            k -> ImmutableMeasurable.builder()
                                    .categoryId(k.get(mc.ID))
                                    .id(k.get(m.ID))
                                    .name(k.get(m.NAME))
                                    .parentId(Optional.ofNullable(k.get(m.PARENT_ID)))
                                    .concrete(k.get(m.CONCRETE))
                                    .organisationalUnitId(k.get(m.ORGANISATIONAL_UNIT_ID))
                                    .lastUpdatedBy(k.get(m.LAST_UPDATED_BY))
                                    .position(k.get(m.POSITION))
                                    .build(),
                            v -> map(v, r -> r.get(parents_with_ratings.field(mr.ENTITY_ID))));

                    Set<MeasurableAlignment> alignments = map(measurableAlignments.entrySet(), es ->
                            ImmutableMeasurableAlignment.builder()
                                    .measurable(es.getKey())
                                    .applicationIds(es.getValue())
                                    .build());

                    return ImmutableMeasurableCategoryAlignment.builder()
                            .categoryReference(categoryRef)
                            .alignments(alignments)
                            .build();
                })
                .collect(Collectors.toSet());

        return categoryAlignments;
    }
}
