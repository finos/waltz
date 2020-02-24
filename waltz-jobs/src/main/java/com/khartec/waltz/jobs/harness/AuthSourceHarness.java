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

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.authoritativesource.AuthoritativeRatingVantagePoint;
import com.khartec.waltz.model.rating.AuthoritativenessRating;
import com.khartec.waltz.schema.tables.EntityHierarchy;
import com.khartec.waltz.schema.tables.LogicalFlowDecorator;
import com.khartec.waltz.schema.tables.records.LogicalFlowDecoratorRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthSourceRatingCalculator;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.function.Function2;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.schema.tables.EntityHierarchy.ENTITY_HIERARCHY;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


public class AuthSourceHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AuthoritativeSourceService svc = ctx.getBean(AuthoritativeSourceService.class);
        AuthSourceRatingCalculator authSourceRatingCalculatorCalculator = ctx.getBean(AuthSourceRatingCalculator.class);
        LogicalFlowDecoratorRatingsCalculator flowCalculator = ctx.getBean(LogicalFlowDecoratorRatingsCalculator.class);
        LogicalFlowDecoratorDao decoratorDao = ctx.getBean(LogicalFlowDecoratorDao.class);
        AuthoritativeSourceDao authoritativeSourceDao = ctx.getBean(AuthoritativeSourceDao.class);


//        EntityHierarchyDao ehDao = ctx.getBean(EntityHierarchyDao.class);
//        List<EntityHierarchyItem> desendents = ehDao.findDesendents(EntityReference.mkRef(EntityKind.DATA_TYPE, 41300));
//        updateDecoratorsForAuthSource(dsl);

        FunctionUtilities.time("Fast Flow Ratings", () -> fastRecalculateAllFlowRatings(dsl, decoratorDao, authoritativeSourceDao));

//        FunctionUtilities.time("Flow ratings", () -> svc.recalculateAllFlowRatings());

        /*
        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints = authoritativeSourceDao.findAuthoritativeRatingVantagePoints(SetUtilities.asSet(6645L));
        List<LogicalFlowDecorator> preDecorators = decoratorDao.findByFlowIds(newArrayList(39835L));
        preDecorators.forEach(System.out::println);

        Collection<LogicalFlowDecorator> postDecorators = flowCalculator.calculate(preDecorators);
        System.out.println("---- calc ----");
        postDecorators.forEach(System.out::println);

        Set<LogicalFlowDecorator> modifiedDecorators = SetUtilities.minus(
                fromCollection(postDecorators),
                fromCollection(preDecorators));

        System.out.println("---- mod ----");
        modifiedDecorators.forEach(System.out::println);
        */

//        System.exit(-1);
    }

    private static void fastRecalculateAllFlowRatings(DSLContext dsl, LogicalFlowDecoratorDao decoratorDao, AuthoritativeSourceDao authoritativeSourceDao) {
        decoratorDao.updateRatingsByCondition(AuthoritativenessRating.NO_OPINION, DSL.trueCondition());

        EntityHierarchy ehOrgUnit = ENTITY_HIERARCHY.as("ehOrgUnit");
        EntityHierarchy ehDataType = ENTITY_HIERARCHY.as("ehDataType");

        Result<Record9<Long, String, String, Long, Integer, String, String, Long, Integer>> authSourcesSpecificFirst = dsl.select(
                AUTHORITATIVE_SOURCE.APPLICATION_ID,
                AUTHORITATIVE_SOURCE.RATING,
                ORGANISATIONAL_UNIT.NAME,
                ORGANISATIONAL_UNIT.ID,
                ehOrgUnit.LEVEL,
                DATA_TYPE.NAME,
                DATA_TYPE.CODE,
                DATA_TYPE.ID,
                ehDataType.LEVEL)
                .from(AUTHORITATIVE_SOURCE)
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID)
                        .and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name())))
                .innerJoin(ehOrgUnit)
                .on(ehOrgUnit.ANCESTOR_ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID)
                        .and(ehOrgUnit.KIND.eq(EntityKind.ORG_UNIT.name()))
                        .and(ehOrgUnit.ID.eq(ehOrgUnit.ANCESTOR_ID)))
                .innerJoin(DATA_TYPE)
                .on(DATA_TYPE.CODE.eq(AUTHORITATIVE_SOURCE.DATA_TYPE))
                .innerJoin(ehDataType)
                    .on(ehDataType.ANCESTOR_ID.eq(DATA_TYPE.ID)
                        .and(ehDataType.KIND.eq(EntityKind.DATA_TYPE.name()))
                        .and(ehDataType.ID.eq(ehDataType.ANCESTOR_ID)))
                .orderBy(
                        ehOrgUnit.LEVEL.desc(),
                        ehDataType.LEVEL.desc(),
                        ORGANISATIONAL_UNIT.ID,
                        DATA_TYPE.NAME
                )
                .fetch();

        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints = authoritativeSourceDao.findAuthoritativeRatingVantagePoints();

        authoritativeRatingVantagePoints.forEach(a -> updateDecoratorsForAuthSource(dsl, a));
    }


    private static int updateDecoratorsForAuthSource(DSLContext dsl, AuthoritativeRatingVantagePoint ratingVantagePoint) {
        System.out.println("Updating decorators for: " + ratingVantagePoint);
        LogicalFlowDecorator lfd = LOGICAL_FLOW_DECORATOR.as("lfd");

        EntityReference vantagePoint = ratingVantagePoint.vantagePoint();
        Long appId = ratingVantagePoint.applicationId();
        EntityReference dataType = ratingVantagePoint.dataType();
        AuthoritativenessRating rating = ratingVantagePoint.rating();

        SelectConditionStep<Record1<Long>> orgUnitSubselect = DSL.select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(vantagePoint.kind().name()))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(vantagePoint.id()));

        SelectConditionStep<Record1<Long>> dataTypeSubselect = DSL.select(ENTITY_HIERARCHY.ID)
                .from(ENTITY_HIERARCHY)
                .where(ENTITY_HIERARCHY.KIND.eq(EntityKind.DATA_TYPE.name()))
                .and(ENTITY_HIERARCHY.ANCESTOR_ID.eq(dataType.id()));


        Condition usingAuthSource = LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(appId);
        Condition notUsingAuthSource = LOGICAL_FLOW.SOURCE_ENTITY_ID.ne(appId);

        Function2<Condition, String, Update<LogicalFlowDecoratorRecord>> mkQuery = (appScopingCondition, ratingName) -> dsl
                .update(LOGICAL_FLOW_DECORATOR)
                .set(LOGICAL_FLOW_DECORATOR.RATING, ratingName)
                .where(LOGICAL_FLOW_DECORATOR.ID.in(
                        DSL.select(lfd.ID)
                                .from(lfd)
                                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.ID.eq(lfd.LOGICAL_FLOW_ID))
                                .innerJoin(APPLICATION)
                                .on(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                                .where(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                                        .and(appScopingCondition)
                                        .and(APPLICATION.ORGANISATIONAL_UNIT_ID.in(orgUnitSubselect))
                                        .and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                                        .and(lfd.DECORATOR_ENTITY_ID.in(dataTypeSubselect)))
                                .and(lfd.RATING.in(AuthoritativenessRating.NO_OPINION.name(), AuthoritativenessRating.DISCOURAGED.name()))

                ));

        Update<LogicalFlowDecoratorRecord> updateAuthSources = mkQuery.apply(usingAuthSource, rating.name());
        Update<LogicalFlowDecoratorRecord> updateNonAuthSources = mkQuery.apply(notUsingAuthSource, AuthoritativenessRating.DISCOURAGED.name());
        int authSourceUpdateCount = updateAuthSources.execute();
        System.out.printf("Updated %s Authoritative decorators for: %s \r\n", authSourceUpdateCount, ratingVantagePoint);
        int nonAuthSourceUpdateCount = updateNonAuthSources.execute();
        System.out.printf("Updated %s Non-Auth decorators for: %s \r\n", nonAuthSourceUpdateCount, ratingVantagePoint);

        return authSourceUpdateCount + nonAuthSourceUpdateCount;
    }


}
