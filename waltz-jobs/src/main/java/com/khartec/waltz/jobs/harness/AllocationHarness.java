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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.measurable_category.MeasurableCategoryDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.allocation_scheme.AllocationScheme;
import com.khartec.waltz.model.allocation_scheme.ImmutableAllocationScheme;
import com.khartec.waltz.model.application.*;
import com.khartec.waltz.model.measurable.ImmutableMeasurable;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_category.MeasurableCategory;
import com.khartec.waltz.model.measurable_rating.ImmutableSaveMeasurableRatingCommand;
import com.khartec.waltz.model.measurable_rating.SaveMeasurableRatingCommand;
import com.khartec.waltz.model.rating.RagRating;
import com.khartec.waltz.schema.tables.records.AllocationRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.allocation_schemes.AllocationSchemeService;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.measurable.MeasurableService;
import com.khartec.waltz.service.measurable_rating.MeasurableRatingService;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple4;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.schema.Tables.*;


public class AllocationHarness {

    private static final String PROVENANCE = "TEST_DATA_321";

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AllocationSchemeService schemesService = ctx.getBean(AllocationSchemeService.class);
        MeasurableService measurableService = ctx.getBean(MeasurableService.class);
        MeasurableCategoryDao categoryDao  = ctx.getBean(MeasurableCategoryDao.class);
        MeasurableRatingService ratingService = ctx.getBean(MeasurableRatingService.class);
        ApplicationService applicationService = ctx.getBean(ApplicationService.class);

        Tuple4<Application, MeasurableCategory, List<Measurable>, AllocationScheme> stuff = setup(
                dsl,
                schemesService,
                measurableService,
                categoryDao,
                applicationService);

        addRating(ratingService, stuff, 0);
        addRating(ratingService, stuff, 1);
//        addRating(ratingService, stuff, 2);

//        addAllocation(dsl, stuff, 0, 50);
        addAllocation(dsl, stuff, 1, 20);

        Long measurableCategory = stuff.v2.id().get();


        dumpRatings(dsl, stuff.v4);
        dumpAllocs(dsl, stuff.v4);

        doDiff(dsl, measurableCategory);

        System.out.println(ratingService.findByCategory(measurableCategory));
    }

    private static void dumpAllocs(DSLContext dsl, AllocationScheme v4) {
        System.out.println("-- ALLOCATIONS ---");
        System.out.println(dsl
                .selectFrom(ALLOCATION)
                .where(ALLOCATION.ALLOCATION_SCHEME_ID.eq(v4.id().get()))
                .fetch());
    }

    private static void dumpRatings(DSLContext dsl, AllocationScheme v4) {
        System.out.println("-- RATINGS ---");
        System.out.println(dsl
                .select()
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE).on(MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .innerJoin(ALLOCATION_SCHEME).on(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(MEASURABLE.MEASURABLE_CATEGORY_ID))
                .where(ALLOCATION_SCHEME.ID.eq(v4.id().get()))
                .fetch());
    }

    private static void addAllocation(DSLContext dsl,
                                      Tuple4<Application, MeasurableCategory, List<Measurable>, AllocationScheme> stuff,
                                      int measurableIdx,
                                      int percentage) {
        AllocationRecord allocationRecord = dsl.newRecord(ALLOCATION);
        allocationRecord.setAllocationSchemeId(stuff.v4.id().get());
        allocationRecord.setEntityId(stuff.v1.entityReference().id());
        allocationRecord.setEntityKind(stuff.v1.entityReference().kind().name());
        allocationRecord.setAllocationPercentage(percentage);
        allocationRecord.setMeasurableId(stuff.v3.get(measurableIdx).id().get());
        allocationRecord.setLastUpdatedBy("admin");
        allocationRecord.setProvenance(PROVENANCE);

        allocationRecord.insert();
    }

    private static void addRating(MeasurableRatingService ratingService, Tuple4<Application, MeasurableCategory, List<Measurable>, AllocationScheme> stuff, int measurableIdx) {
        SaveMeasurableRatingCommand command1 = ImmutableSaveMeasurableRatingCommand.builder()
                .entityReference(stuff.v1.entityReference())
                .measurableId(stuff.v3.get(measurableIdx).id().get())
                .provenance(PROVENANCE)
                .rating('G')
                .lastUpdate(UserTimestamp.mkForUser("admin"))
                .build();

        ratingService.save(command1, false);
    }

    private static void doDiff(DSLContext dsl, Long measurableCategory) {
        calcAdditions(dsl, measurableCategory);
        calcRemovals(dsl, measurableCategory);
    }

    private static void calcRemovals(DSLContext dsl, Long measurableCategory) {
        Table<Record3<Long, Long, String>> obsolete = mkExistingAllocsQry(measurableCategory)
                .except(mkExistingRatingsQet(measurableCategory))
                .asTable("obsolete");

        SelectOnConditionStep<Record> qry = dsl
                .select(ALLOCATION_SCHEME.ID.as("allocation_scheme_id"))
                .select(obsolete.asterisk())
                .select(DSL.val("REMOVE").as("op"))
                .from(obsolete)
                .innerJoin(ALLOCATION_SCHEME).on(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(measurableCategory));

        System.out.println("-- REMOVALS --");
        System.out.println(qry.fetch());

    }


    private static void calcAdditions(DSLContext dsl, Long measurableCategory) {
        Table<Record3<Long, Long, String>> additions = mkExistingRatingsQet(measurableCategory)
                .except(mkExistingAllocsQry(measurableCategory))
                .asTable("additions");

        SelectOnConditionStep<Record> qry = dsl
                .select(ALLOCATION_SCHEME.ID.as("allocation_scheme_id"))
                .select(additions.asterisk())
                .select(DSL.val("ADD").as("op"))
                .from(additions)
                .innerJoin(ALLOCATION_SCHEME).on(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(measurableCategory));

        System.out.println("-- ADDITIONS --");
        System.out.println(qry.fetch());

    }

    private static SelectConditionStep<Record3<Long, Long, String>> mkExistingAllocsQry(Long categoryId) {
        return DSL
                .select(ALLOCATION.MEASURABLE_ID,
                        ALLOCATION.ENTITY_ID,
                        ALLOCATION.ENTITY_KIND)
                .from(ALLOCATION)
                .innerJoin(ALLOCATION_SCHEME).on(ALLOCATION_SCHEME.ID.eq(ALLOCATION.ALLOCATION_SCHEME_ID))
                .where(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(categoryId));
    }


    private static Tuple4<Application, MeasurableCategory, List<Measurable>, AllocationScheme> setup(DSLContext dsl,
                                                                                                     AllocationSchemeService schemesService,
                                                                                                     MeasurableService measurableService,
                                                                                                     MeasurableCategoryDao categoryDao,
                                                                                                     ApplicationService applicationService) {
        deleteAll(dsl);
        Application app = mkApp(dsl, applicationService);
        MeasurableCategory category = mkCategory(dsl, categoryDao);
        List<Measurable> measurables = mkMeasurables(measurableService, category);
        AllocationScheme scheme = mkScheme(schemesService, category);

        return Tuple.tuple(app,category,measurables,scheme);
    }


    private static SelectConditionStep<Record3<Long, Long, String>> mkExistingRatingsQet(Long measurableCategory) {
        return DSL
                .select(MEASURABLE_RATING.MEASURABLE_ID,
                        MEASURABLE_RATING.ENTITY_ID,
                        MEASURABLE_RATING.ENTITY_KIND)
                .from(MEASURABLE_RATING)
                .innerJoin(MEASURABLE).on(MEASURABLE.ID.eq(MEASURABLE_RATING.MEASURABLE_ID))
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(measurableCategory));
    }


    private static AllocationScheme mkScheme(AllocationSchemeService schemeService, MeasurableCategory category) {
        AllocationScheme scheme = ImmutableAllocationScheme.builder()
                .description(PROVENANCE)
                .name("TEST_SCHEME")
                .measurableCategoryId(category.id().get())
                .build();

        long id = schemeService.create(scheme);

        return schemeService.getById(id);
    }


    private static List<Measurable> mkMeasurables(MeasurableService measurableService, MeasurableCategory category) {
        Measurable m = ImmutableMeasurable.builder()
                .name("z")
                .categoryId(category.id().get())
                .concrete(true)
                .provenance(PROVENANCE)
                .lastUpdatedBy("admin")
                .build();

        measurableService.create(ImmutableMeasurable.copyOf(m).withName("A"), "admin");
        measurableService.create(ImmutableMeasurable.copyOf(m).withName("B"), "admin");
        measurableService.create(ImmutableMeasurable.copyOf(m).withName("C"), "admin");

        return measurableService.findByCategoryId(category.id().get());
    }

    private static MeasurableCategory mkCategory(DSLContext dsl, MeasurableCategoryDao categoryDao) {
        Long categoryId = dsl.insertInto(MEASURABLE_CATEGORY)
                .set(MEASURABLE_CATEGORY.NAME, "Test")
                .set(MEASURABLE_CATEGORY.RATING_SCHEME_ID, 1L)
                .set(MEASURABLE_CATEGORY.LAST_UPDATED_BY, "admin")
                .set(MEASURABLE_CATEGORY.DESCRIPTION, PROVENANCE)
                .set(MEASURABLE_CATEGORY.EXTERNAL_ID, PROVENANCE)
                .returning(MEASURABLE_CATEGORY.ID)
                .fetchOne()
                .getId();

        return categoryDao.getById(categoryId);
    }


    private static Application mkApp(DSLContext dsl, ApplicationService applicationService) {
        AppRegistrationRequest req = ImmutableAppRegistrationRequest.builder()
                .name("TEST APP")
                .organisationalUnitId(10L)
                .businessCriticality(Criticality.UNKNOWN)
                .applicationKind(ApplicationKind.EUC)
                .lifecyclePhase(LifecyclePhase.PRODUCTION)
                .overallRating(RagRating.G)
                .build();
        AppRegistrationResponse resp = applicationService.registerApp(req, "admin");
        dsl.update(APPLICATION)
                .set(APPLICATION.PROVENANCE, PROVENANCE)
                .where(APPLICATION.ID.eq(resp.id().get()))
                .execute();

        return applicationService.getById(resp.id().get());
    }


    private static void deleteAll(DSLContext dsl) {
        dsl.deleteFrom(APPLICATION).where(APPLICATION.PROVENANCE.eq(PROVENANCE)).execute();
        dsl.deleteFrom(MEASURABLE).where(MEASURABLE.PROVENANCE.eq(PROVENANCE)).execute();
        dsl.deleteFrom(MEASURABLE_CATEGORY).where(MEASURABLE_CATEGORY.EXTERNAL_ID.eq(PROVENANCE)).execute();
        dsl.deleteFrom(ALLOCATION_SCHEME).where(ALLOCATION_SCHEME.DESCRIPTION.eq(PROVENANCE)).execute();
        dsl.deleteFrom(ALLOCATION).where(ALLOCATION.PROVENANCE.eq(PROVENANCE)).execute();
    }
}
