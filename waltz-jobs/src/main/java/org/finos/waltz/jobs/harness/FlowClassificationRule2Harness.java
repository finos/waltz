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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.common.LoggingUtilities;
import org.finos.waltz.common.MapUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.Nullable;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.EntityHierarchy;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.LogicalFlowDecorator;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleService;
import org.immutables.value.Value;
import org.jooq.DSLContext;
import org.jooq.lambda.function.Function4;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.data.JooqUtilities.readRef;
import static org.jooq.lambda.tuple.Tuple.tuple;


public class FlowClassificationRule2Harness {

    public static final Logger LOG = LoggerFactory.getLogger(FlowClassificationRule2Harness.class);


    @Value.Immutable
    interface FlowDataType {
        EntityReference source();
        EntityReference target();
        @Nullable Long sourceOuId();
        @Nullable Long targetOuId();
        long lfId();
        long lfdId();
        long dtId();
        @Nullable Long outboundRuleId();
        @Nullable Long inboundRuleId();
    }


    private static final LogicalFlow lf = Tables.LOGICAL_FLOW;
    private static final LogicalFlowDecorator lfd = Tables.LOGICAL_FLOW_DECORATOR;
    private static final Application srcApp = Tables.APPLICATION.as("srcApp");
    private static final Application targetApp = Tables.APPLICATION.as("targetApp");
    private static final EntityHierarchy eh = Tables.ENTITY_HIERARCHY;


    public static void main(String[] args) {
        LoggingUtilities.configureLogging();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        FlowClassificationRuleService svc = ctx.getBean(FlowClassificationRuleService.class);
        FlowClassificationRuleDao dao = ctx.getBean(FlowClassificationRuleDao.class);

        FunctionUtilities.time("doIt", () -> doIt(dsl, dao));

//        System.exit(-1);
    }


    private static void doIt(DSLContext dsl,
                             FlowClassificationRuleDao dao) {
        LOG.debug("Loading rule vantage points");

        List<FlowClassificationRuleVantagePoint> inboundRuleVantagePoints = dao.findFlowClassificationRuleVantagePoints(FlowDirection.INBOUND);
        List<FlowClassificationRuleVantagePoint> outboundRuleVantagePoints = dao.findFlowClassificationRuleVantagePoints(FlowDirection.OUTBOUND);


//        take(allRuleVantagePoints, 10).forEach(System.out::println);

        Set<FlowDataType> population = fetchFlowDataTypePopulation(dsl);

        LOG.debug(
                "Loaded: {} inbound and {} outbound vantage point rules, and a population of: {} flows with datatypes",
                inboundRuleVantagePoints.size(),
                outboundRuleVantagePoints.size(),
                population.size());

        //        take(population, 10).forEach(System.out::println);

        LOG.debug("Loading hierarchies");
        List<Tuple2<Long, Long>> ouHierarchy = fetchHierarchy(dsl, EntityKind.ORG_UNIT);
        List<Tuple2<Long, Long>> dtHierarchy = fetchHierarchy(dsl, EntityKind.DATA_TYPE);
//
//        System.out.println(findChildren(ouHierarchy, 10186L));
//        System.out.println(findChildren(dtHierarchy, 33100L));

        LOG.debug("Applying rules to population");
        Map<Long, Tuple2<Long, MatchOutcome>> lfdIdToOutboundRuleIdMap = applyVantagePoints(FlowDirection.OUTBOUND, outboundRuleVantagePoints, population, ouHierarchy, dtHierarchy);
        Map<Long, Tuple2<Long, MatchOutcome>> lfdIdToInboundRuleIdMap = applyVantagePoints(FlowDirection.INBOUND, inboundRuleVantagePoints, population, ouHierarchy, dtHierarchy);

        System.out.println("Curr");
        MapUtilities.countBy(FlowDataType::outboundRuleId, SetUtilities.filter(population, p -> p.outboundRuleId() != null)).entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(20).forEach(System.out::println);
        System.out.println("Future");
        MapUtilities.countBy(Map.Entry::getValue, lfdIdToOutboundRuleIdMap.entrySet()).entrySet().stream().sorted(Map.Entry.comparingByKey()).limit(20).forEach(System.out::println);

    }


    private static Map<Long, Tuple2<Long, MatchOutcome>> applyVantagePoints(FlowDirection direction,
                                                                            List<FlowClassificationRuleVantagePoint> ruleVantagePoints,
                                                                            Set<FlowDataType> population,
                                                                            List<Tuple2<Long, Long>> ouHierarchy,
                                                                            List<Tuple2<Long, Long>> dtHierarchy) {

        Function4<FlowClassificationRuleVantagePoint, Set<Long>, Set<Long>, FlowDataType, MatchOutcome> matcher = determineMatcherFn(direction);

        Map<Long, Tuple2<Long, MatchOutcome>> lfdIdToRuleAndOutcomeMap = new HashMap<>();
        ruleVantagePoints
                .stream()
                .filter(rvp -> rvp.vantagePoint().kind() == EntityKind.ORG_UNIT)
                .forEach(rvp -> {
                    Set<Long> childOUs = findChildren(ouHierarchy, rvp.vantagePoint().id());
                    Set<Long> childDTs = findChildren(dtHierarchy, rvp.dataType().id());
                    population.forEach(p -> {
                        Tuple2<Long, MatchOutcome> currentRuleAndOutcome = lfdIdToRuleAndOutcomeMap.get(p.lfdId());
                        if (currentRuleAndOutcome != null && currentRuleAndOutcome.v2 == MatchOutcome.POSITIVE_MATCH) {
                            return; // skip, already got a good match
                        }
                        MatchOutcome outcome = matcher.apply(rvp, childOUs, childDTs, p);
                        if (outcome == MatchOutcome.NOT_APPLICABLE) {
                            // skip
                        } else if (currentRuleAndOutcome == null) {
                            lfdIdToRuleAndOutcomeMap.put(p.lfdId(), tuple(rvp.ruleId(), outcome));
                        } else if (currentRuleAndOutcome.v2 == MatchOutcome.NEGATIVE_MATCH && outcome == MatchOutcome.POSITIVE_MATCH) {
                            // override result as we have a positive match
                            lfdIdToRuleAndOutcomeMap.put(p.lfdId(), tuple(rvp.ruleId(), MatchOutcome.POSITIVE_MATCH));
                        } else {
                            // skip, leave the map alone as a more specific negative rule id already exists
                        }
                    });
                });

        LOG.debug(
                "finished processing {} {} rules, {} decorators have outcomes",
                ruleVantagePoints.size(),
                direction,
                lfdIdToRuleAndOutcomeMap.size());

        return lfdIdToRuleAndOutcomeMap;
    }

    enum MatchOutcome {
        NOT_APPLICABLE,
        NEGATIVE_MATCH,
        POSITIVE_MATCH

    }

    private static Function4<FlowClassificationRuleVantagePoint, Set<Long>, Set<Long>, FlowDataType, MatchOutcome> determineMatcherFn(FlowDirection direction) {
        Function4<FlowClassificationRuleVantagePoint,  Set<Long>,  Set<Long>, FlowDataType, MatchOutcome> inboundMatcher =
                (rvp, childOUs, childDTs, p) -> {
                    boolean subjectMatches = p.target().equals(rvp.subjectReference());
                    boolean dtAndOuMatches = childDTs.contains(p.dtId()) && rvp.vantagePoint().kind() == EntityKind.ORG_UNIT && p.sourceOuId() != null && childOUs.contains(p.sourceOuId());
                    return determineOutcome(subjectMatches, dtAndOuMatches);
                };

        Function4<FlowClassificationRuleVantagePoint,  Set<Long>,  Set<Long>, FlowDataType, MatchOutcome> outboundMatcher =
                (rvp, childOUs, childDTs, p) -> {
                    boolean subjectMatches = p.source().equals(rvp.subjectReference());
                    boolean dtAndOuMatches = childDTs.contains(p.dtId()) && rvp.vantagePoint().kind() == EntityKind.ORG_UNIT && p.targetOuId() != null && childOUs.contains(p.targetOuId());
                    return determineOutcome(subjectMatches, dtAndOuMatches);
                };

        return direction == FlowDirection.INBOUND
                ? inboundMatcher
                : outboundMatcher;
    }


    private static MatchOutcome determineOutcome(boolean subjectMatches,
                                                 boolean dtAndOuMatches) {
        if (subjectMatches && dtAndOuMatches) {
            return MatchOutcome.POSITIVE_MATCH;
        } else if (dtAndOuMatches) {
            return MatchOutcome.NEGATIVE_MATCH;
        } else {
            return MatchOutcome.NOT_APPLICABLE;
        }
    }



    private static Set<FlowDataType> fetchFlowDataTypePopulation(DSLContext dsl) {
        LOG.debug("Loading population");
        return dsl
                .select(lf.ID,
                        lfd.ID, lfd.DECORATOR_ENTITY_ID, lfd.INBOUND_FLOW_CLASSIFICATION_RULE_ID, lfd.FLOW_CLASSIFICATION_RULE_ID,
                        lf.SOURCE_ENTITY_ID, lf.SOURCE_ENTITY_KIND, lf.TARGET_ENTITY_ID, lf.TARGET_ENTITY_KIND,
                        srcApp.ORGANISATIONAL_UNIT_ID,
                        targetApp.ORGANISATIONAL_UNIT_ID)
                .from(lf)
                .innerJoin(lfd).on(lfd.LOGICAL_FLOW_ID.eq(lf.ID).and(lfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .leftJoin(srcApp).on(srcApp.ID.eq(lf.SOURCE_ENTITY_ID).and(lf.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(targetApp).on(targetApp.ID.eq(lf.TARGET_ENTITY_ID).and(lf.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(lf.IS_REMOVED.isFalse()
                    .and(lf.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name())))
                .fetchSet(r -> ImmutableFlowDataType
                        .builder()
                        .lfdId(r.get(lfd.ID))
                        .dtId(r.get(lfd.DECORATOR_ENTITY_ID))
                        .lfId(r.get(lf.ID))
                        .source(readRef(r, lf.SOURCE_ENTITY_KIND, lf.SOURCE_ENTITY_ID))
                        .target(readRef(r, lf.TARGET_ENTITY_KIND, lf.TARGET_ENTITY_ID))
                        .inboundRuleId(r.get(lfd.INBOUND_FLOW_CLASSIFICATION_RULE_ID))
                        .outboundRuleId(r.get(lfd.FLOW_CLASSIFICATION_RULE_ID))
                        .sourceOuId(r.get(srcApp.ORGANISATIONAL_UNIT_ID))
                        .targetOuId(r.get(targetApp.ORGANISATIONAL_UNIT_ID))
                        .build());
    }


    private static List<Tuple2<Long, Long>> fetchHierarchy(DSLContext dsl,
                                                           EntityKind kind) {
        return dsl
                .select(eh.ID, eh.ANCESTOR_ID)
                .from(eh)
                .where(eh.KIND.eq(kind.name()))
                .fetch(r -> tuple(r.get(eh.ID), r.get(eh.ANCESTOR_ID)));
    }


    private static Set<Long> findChildren(List<Tuple2<Long, Long>> hierarchy,
                                          long parentId) {
        return hierarchy
                .stream()
                .filter(t -> t.v2 == parentId)
                .map(t -> t.v1)
                .collect(Collectors.toSet());
    }


}
