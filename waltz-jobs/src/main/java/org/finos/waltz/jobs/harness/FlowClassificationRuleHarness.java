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

import org.finos.waltz.data.datatype_decorator.LogicalFlowDecoratorDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import org.finos.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.datatype.FlowDataType;
import org.finos.waltz.model.entity_hierarchy.EntityHierarchy;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleVantagePoint;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.finos.waltz.service.flow_classification_rule.FlowClassificationRuleService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.FunctionUtilities.time;


public class FlowClassificationRuleHarness {

    private static final LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory = new LogicalFlowIdSelectorFactory();


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);


        FlowClassificationRuleService fcrSvc = ctx.getBean(FlowClassificationRuleService.class);
        EntityHierarchyService ehSvc = ctx.getBean(EntityHierarchyService.class);
        FlowClassificationRuleDao fcrDao = ctx.getBean(FlowClassificationRuleDao.class);
        LogicalFlowDecoratorDao lfdDao = ctx.getBean(LogicalFlowDecoratorDao.class);

        EntityReference group = EntityReference.mkRef(EntityKind.ORG_UNIT, 99L);

        Select<Record1<Long>> flowSelector = mkSelector(group);
        EntityHierarchy dtHierarchy = time("loading dt hier", () -> ehSvc.fetchHierarchyForKind(EntityKind.DATA_TYPE));
        Set<FlowDataType> population = time("lfd::fetchPopulation", () -> lfdDao.fetchFlowDataTypePopulationForFlowSelector(flowSelector));
        Set<Long> possibleDtIds = population
                .stream()
                .map(FlowDataType::dtId)
                .distinct()
                .flatMap(dtId -> dtHierarchy.findAncestors(dtId).stream())
                .collect(Collectors.toSet());

        List<FlowClassificationRuleVantagePoint> allRules = time("fcrDao::findRules (all)", () -> fcrDao.findFlowClassificationRuleVantagePoints(FlowDirection.OUTBOUND));
        List<FlowClassificationRuleVantagePoint> targetedDtRules = time("fcrDao::findRules (targeted - dt)", () -> fcrDao.findFlowClassificationRuleVantagePoints(FlowDirection.OUTBOUND, possibleDtIds));
        List<FlowClassificationRuleVantagePoint> targetedPopRules = time("fcrDao::findRules (targeted- pop)", () -> fcrDao.findFlowClassificationRuleVantagePoints(FlowDirection.OUTBOUND, dtHierarchy, population));
        List<FlowClassificationRuleVantagePoint> targetedInboundPopRules = time("fcrDao::findRules (targeted- inbound pop)", () -> fcrDao.findFlowClassificationRuleVantagePoints(FlowDirection.INBOUND, dtHierarchy, population));

        System.out.printf(
                "\nRules: pop:%d / dt:%d / all:%d, Population: %d, DTs: %d\n\n",
                targetedPopRules.size(),
                targetedDtRules.size(),
                allRules.size(),
                population.size(),
                possibleDtIds.size());

        time("recalc", () -> fcrSvc.recalculateRatingsForPopulation(population));
    }

    private static Select<Record1<Long>> mkSelector(EntityReference ref) {
        IdSelectionOptions opts = IdSelectionOptions.mkOpts(ref);
        Select<Record1<Long>> flowSelector = logicalFlowIdSelectorFactory.apply(opts);
        return flowSelector;
    }


}
