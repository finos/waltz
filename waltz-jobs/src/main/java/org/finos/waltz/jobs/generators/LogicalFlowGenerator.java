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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import org.finos.waltz.data.logical_flow.LogicalFlowDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.application.Application;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRule;
import org.finos.waltz.model.logical_flow.ImmutableLogicalFlow;
import org.finos.waltz.model.logical_flow.LogicalFlow;
import org.finos.waltz.model.orgunit.OrganisationalUnit;
import org.finos.waltz.schema.tables.records.LogicalFlowRecord;
import org.finos.waltz.service.application.ApplicationService;
import org.finos.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.common.RandomUtilities.randomlySizedIntStream;
import static org.finos.waltz.common.SetUtilities.uniqBy;
import static org.finos.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;


public class LogicalFlowGenerator implements SampleDataGenerator {


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        FlowClassificationRuleDao flowClassificationRuleDao = ctx.getBean(FlowClassificationRuleDao.class);
        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        OrganisationalUnitService orgUnitDao = ctx.getBean(OrganisationalUnitService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<FlowClassificationRule> flowClassificationRules = flowClassificationRuleDao.findByEntityKind(EntityKind.ORG_UNIT);
        List<Application> apps = applicationDao.findAll();
        List<OrganisationalUnit> orgUnits = orgUnitDao.findAll();

        LocalDateTime now = LocalDateTime.now();

        Set<LogicalFlow> expectedFlows = flowClassificationRules
                .stream()
                .flatMap(a -> {
                    long orgUnitId = a.vantagePointReference().id();

                    return randomlySizedIntStream(0, 40)
                            .mapToObj(i ->
                                    randomAppPick(apps, orgUnitId)
                                        .map(target -> ImmutableLogicalFlow.builder()
                                                .source(a.subjectReference())
                                                .target(target)
                                                .lastUpdatedBy("admin")
                                                .provenance(SAMPLE_DATA_PROVENANCE)
                                                .lastUpdatedAt(now)
                                                .build())
                                        .orElse(null));
                })
                .filter(Objects::nonNull)
                .collect(toSet());


        Set<LogicalFlow> probableFlows = flowClassificationRules
                .stream()
                .flatMap(a -> randomlySizedIntStream(0, 30)
                        .mapToObj(i -> randomAppPick(apps, randomPick(orgUnits).id().get())
                                .map(target -> ImmutableLogicalFlow.builder()
                                        .source(a.subjectReference())
                                        .target(target)
                                        .lastUpdatedBy("admin")
                                        .provenance(SAMPLE_DATA_PROVENANCE)
                                        .lastUpdatedAt(now)
                                        .build())
                                .orElse(null)))
                .filter(Objects::nonNull)
                .collect(toSet());


        Set<LogicalFlow> randomFlows = apps
                .stream()
                .flatMap(a -> randomlySizedIntStream(0, 5)
                        .mapToObj(i ->
                            randomAppPick(apps, randomPick(orgUnits).id().get())
                                    .map(target -> ImmutableLogicalFlow.builder()
                                        .source(a.entityReference())
                                        .target(target)
                                        .lastUpdatedBy("admin")
                                        .provenance(SAMPLE_DATA_PROVENANCE)
                                        .lastUpdatedAt(now)
                                        .build())
                                    .orElse(null)))
                .filter(Objects::nonNull)
                .collect(toSet());


        Set<LogicalFlow> all = new HashSet<>();
        all.addAll(randomFlows);
        all.addAll(expectedFlows);
        all.addAll(probableFlows);

        Set<LogicalFlow> deduped = uniqBy(
                all,
                x -> Tuple.tuple(x.source(), x.target()));

        log("--- saving: " + deduped.size());

        Set<LogicalFlowRecord> records = SetUtilities.map(deduped, df -> LogicalFlowDao.TO_RECORD_MAPPER.apply(df, dsl));
        dsl.batchStore(records).execute();

        log("--- done");

        return null;

    }


    private static Optional<EntityReference> randomAppPick(List<Application> apps, long orgUnitId) {
        List<Application> appsForOU = apps
                .stream()
                .filter(a -> a.organisationalUnitId() == orgUnitId)
                .collect(toList());
        return ListUtilities.isEmpty(appsForOU)
                ? Optional.empty()
                : Optional.of(randomPick(appsForOU).entityReference());
    }



    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return true;
    }
}
