/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.jobs.sample;

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
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;


public class AdditiveFlowGenerator {

    private static final Random rnd = new Random();
    private static final long APPLICATION_ID = 67189;
    private static final int APPROX_FLOW_GENERATION_COUNT = 800;


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AuthoritativeSourceDao authSourceDao = ctx.getBean(AuthoritativeSourceDao.class);
        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        LogicalFlowService dataFlowDao = ctx.getBean(LogicalFlowService.class);
        OrganisationalUnitService orgUnitDao = ctx.getBean(OrganisationalUnitService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<AuthoritativeSource> authSources = authSourceDao.findByEntityKind(EntityKind.ORG_UNIT);
        List<Application> apps = applicationDao.findAll();
        Application referenceApplication = applicationDao.getById(APPLICATION_ID);
        List<OrganisationalUnit> orgUnits = orgUnitDao.findAll();


        Set<LogicalFlow> expectedFlows = authSources.stream()
                .map(a -> {
                    long orgUnitId = a.parentReference().id();

                    if(referenceApplication.organisationalUnitId().equals(orgUnitId)){
                        return Optional.of(ImmutableLogicalFlow.builder()
                                .source(a.applicationReference())
                                .target(referenceApplication.entityReference())
                                .build());
                    } else {
                        return Optional.<LogicalFlow>empty();
                    }
                })
                .filter(o -> o.isPresent())
                .map(o -> o.get())
                .collect(toSet());


        Set<LogicalFlow> randomTargetFlows = IntStream
                .range(0, rnd.nextInt(APPROX_FLOW_GENERATION_COUNT /2))
                .mapToObj(i -> {
                    EntityReference target = randomAppPick(apps, randomPick(orgUnits).id().get());
                    return ImmutableLogicalFlow.builder()
                            .source(referenceApplication.entityReference())
                            .target(target)
                            .build();
                })
                .collect(toSet());


        Set<LogicalFlow> randomSourceFlows = IntStream
                .range(0, rnd.nextInt(APPROX_FLOW_GENERATION_COUNT /2))
                .mapToObj(i -> {
                    EntityReference source = randomAppPick(apps, randomPick(orgUnits).id().get());
                    return ImmutableLogicalFlow.builder()
                            .source(source)
                            .target(referenceApplication.entityReference())
                            .build();
                })
                .collect(toSet());


        dsl.delete(LOGICAL_FLOW)
                .where(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(APPLICATION_ID))
                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(APPLICATION_ID))
                .execute();

        Set<LogicalFlow> all = new HashSet<>();
        all.addAll(randomTargetFlows);
        all.addAll(randomSourceFlows);
        all.addAll(expectedFlows);

        System.out.println("--- saving: " + all.size());

        Set<LogicalFlowRecord> records = SetUtilities.map(all, df -> LogicalFlowDao.TO_RECORD_MAPPER.apply(df, dsl));
        dsl.batchStore(records).execute();

        System.out.println("--- done");


    }


    private static EntityReference randomAppPick(List<Application> apps, long orgUnitId) {
        List<Application> appsForOU = apps.stream()
                .filter(a -> a.organisationalUnitId() == orgUnitId)
                .collect(toList());
        return randomPick(appsForOU).entityReference();
    }
}
