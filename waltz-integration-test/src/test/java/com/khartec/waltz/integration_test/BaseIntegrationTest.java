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

package com.khartec.waltz.integration_test;

import com.khartec.waltz.common.CollectionUtilities;
import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.actor.ImmutableActorCreateCommand;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.schema.tables.records.*;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.junit.BeforeClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static com.khartec.waltz.schema.Tables.*;

public class BaseIntegrationTest {

    protected static ApplicationContext ctx;

    private AtomicLong ctr = new AtomicLong(1_000_000);

    @BeforeClass
    public static void baseSetUp() {
        LoggingUtilities.configureLogging();
        ctx = new AnnotationConfigApplicationContext(DITestingConfiguration.class);
    }


    public LogicalFlow createLogicalFlow(EntityReference refA, EntityReference refB) {
        LogicalFlowDao dao = ctx.getBean(LogicalFlowDao.class);
        return dao.addFlow(ImmutableLogicalFlow
                .builder()
                .source(refA)
                .target(refB)
                .lastUpdatedBy("admin")
                .build());
    }


    public Long createActor(String nameStem) {
        ActorDao dao = ctx.getBean(ActorDao.class);
        return dao.create(
                ImmutableActorCreateCommand
                        .builder()
                        .name(nameStem + "Name")
                        .description(nameStem + "Desc")
                        .isExternal(true)
                        .build(),
                "admin");
    }


    protected long createMeasurableCategory(String testCategory) {
        MeasurableCategoryDao dao = ctx.getBean(MeasurableCategoryDao.class);
        Set<MeasurableCategory> categories = dao.findByExternalId(testCategory);
        return CollectionUtilities
                .maybeFirst(categories)
                .map(c -> c.id().get())
                .orElseGet(() -> {
                    long schemeId = createEmptyRatingScheme("test");
                    MeasurableCategoryRecord record = getDsl().newRecord(MEASURABLE_CATEGORY);
                    record.setDescription(testCategory);
                    record.setName(testCategory);
                    record.setExternalId(testCategory);
                    record.setRatingSchemeId(schemeId);
                    record.setLastUpdatedBy("admin");
                    record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    record.setEditable(false);
                    record.store();
                    return record.getId();
                });
    }


    private long createEmptyRatingScheme(String name) {
        DSLContext dsl = getDsl();
        return dsl
                .select(RATING_SCHEME.ID)
                .from(RATING_SCHEME)
                .where(RATING_SCHEME.NAME.eq(name))
                .fetchOptional(RATING_SCHEME.ID)
                .orElseGet(() -> {
                    RatingSchemeRecord record = dsl.newRecord(RATING_SCHEME);
                    record.setName(name);
                    record.setDescription(name);
                    record.store();
                    return record.getId();
                });
    }


    protected long createMeasurable(String name, long categoryId) {
        return getDsl()
                .select(MEASURABLE.ID)
                .from(MEASURABLE)
                .where(MEASURABLE.EXTERNAL_ID.eq(name))
                .and(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetchOptional(MEASURABLE.ID)
                .orElseGet(() -> {
                    MeasurableRecord record = getDsl().newRecord(MEASURABLE);
                    record.setMeasurableCategoryId(categoryId);
                    record.setName(name);
                    record.setDescription(name);
                    record.setConcrete(true);
                    record.setExternalId(name);
                    record.setProvenance("test");
                    record.setLastUpdatedBy("test");
                    record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    record.store();
                    return record.getId();
                });
    }


    protected long createChangeInitiative(String name) {
        return getDsl()
                .select(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.EXTERNAL_ID.eq(name))
                .fetchOptional(CHANGE_INITIATIVE.ID)
                .orElseGet(() -> {
                    ChangeInitiativeRecord record = getDsl().newRecord(CHANGE_INITIATIVE);
                    record.setName(name);
                    record.setDescription(name);
                    record.setExternalId(name);
                    record.setProvenance("test");
                    record.setId(ctr.incrementAndGet());
                    record.setStartDate(DateTimeUtilities.toSqlDate(LocalDate.now()));
                    record.setEndDate(DateTimeUtilities.toSqlDate(LocalDate.now()));

                    record.store();
                    return record.getId();
                });
    }


    protected long createRelationshipKind(String name, EntityKind kindA, EntityKind kindB) {
        return getDsl()
                .select(RELATIONSHIP_KIND.ID)
                .from(RELATIONSHIP_KIND)
                .where(RELATIONSHIP_KIND.NAME.eq(name))
                .fetchOptional(RELATIONSHIP_KIND.ID)
                .orElseGet(() -> {
                    RelationshipKindRecord record = getDsl().newRecord(RELATIONSHIP_KIND);
                    record.setKindA(kindA.name());
                    record.setKindB(kindB.name());
                    record.setName(name);
                    record.setDescription(name);

                    record.store();
                    return record.getId();
                });
    }



    @NotNull
    public DSLContext getDsl() {
        return ctx.getBean(DSLContext.class);
    }
}

