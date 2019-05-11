/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.schema.tables.records.LogicalFlowRecord;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.util.*;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.common.RandomUtilities.randomlySizedIntStream;
import static com.khartec.waltz.common.SetUtilities.uniqBy;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;


public class LogicalFlowGenerator implements SampleDataGenerator {


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        AuthoritativeSourceDao authSourceDao = ctx.getBean(AuthoritativeSourceDao.class);
        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        OrganisationalUnitService orgUnitDao = ctx.getBean(OrganisationalUnitService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<AuthoritativeSource> authSources = authSourceDao.findByEntityKind(EntityKind.ORG_UNIT);
        List<Application> apps = applicationDao.findAll();
        List<OrganisationalUnit> orgUnits = orgUnitDao.findAll();

        LocalDateTime now = LocalDateTime.now();

        Set<LogicalFlow> expectedFlows = authSources.stream()
                .flatMap(a -> {
                    long orgUnitId = a.parentReference().id();

                    return randomlySizedIntStream(0, 40)
                            .mapToObj(i ->
                                    randomAppPick(apps, orgUnitId)
                                        .map(target -> ImmutableLogicalFlow.builder()
                                                .source(a.applicationReference())
                                                .target(target)
                                                .lastUpdatedBy("admin")
                                                .provenance(SAMPLE_DATA_PROVENANCE)
                                                .lastUpdatedAt(now)
                                                .build())
                                        .orElse(null));
                })
                .filter(Objects::nonNull)
                .collect(toSet());


        Set<LogicalFlow> probableFlows = authSources.stream()
                .flatMap(a -> randomlySizedIntStream(0, 30)
                        .mapToObj(i -> randomAppPick(apps, randomPick(orgUnits).id().get())
                                .map(target -> ImmutableLogicalFlow.builder()
                                        .source(a.applicationReference())
                                        .target(target)
                                        .lastUpdatedBy("admin")
                                        .provenance(SAMPLE_DATA_PROVENANCE)
                                        .lastUpdatedAt(now)
                                        .build())
                                .orElse(null)))
                .filter(Objects::nonNull)
                .collect(toSet());


        Set<LogicalFlow> randomFlows = apps.stream()
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
