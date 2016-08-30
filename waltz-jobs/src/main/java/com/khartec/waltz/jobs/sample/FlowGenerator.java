/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.authoritativesource.AuthoritativeSource;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.data_flow.DataFlowService;
import com.khartec.waltz.service.data_type.DataTypeService;
import com.khartec.waltz.service.orgunit.OrganisationalUnitService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static java.util.stream.Collectors.toList;


public class FlowGenerator {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        AuthoritativeSourceDao authSourceDao = ctx.getBean(AuthoritativeSourceDao.class);
        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        DataTypeService dataTypesDao = ctx.getBean(DataTypeService.class);
        DataFlowService dataFlowDao = ctx.getBean(DataFlowService.class);
        OrganisationalUnitService orgUnitDao = ctx.getBean(OrganisationalUnitService.class);
        DataSource dataSource = ctx.getBean(DataSource.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<AuthoritativeSource> authSources = authSourceDao.findByEntityKind(EntityKind.ORG_UNIT);
        List<String> dataTypes = dataTypesDao.getAll().stream().map(dt -> dt.code()).collect(toList());
        List<Application> apps = applicationDao.findAll();
        List<OrganisationalUnit> orgUnits = orgUnitDao.findAll();


//        Set<DataFlow> expectedFlows = authSources.stream()
//                .flatMap(a -> {
//                    long orgUnitId = a.parentReference().id();
//
//                    return IntStream.range(0, rnd.nextInt(40))
//                            .mapToObj(i -> ImmutableDataFlow.builder()
//                                    .dataType(a.dataType())
//                                    .source(a.applicationReference())
//                                    .target(randomAppPick(apps, orgUnitId))
//                                    .build());
//                })
//                .collect(Collectors.toSet());


//        Set<DataFlow> probableFlows = authSources.stream()
//                .flatMap(a -> IntStream.range(0, rnd.nextInt(30))
//                        .mapToObj(i -> ImmutableDataFlow.builder()
//                                .dataType(a.dataType())
//                                .source(a.applicationReference())
//                                .target(randomAppPick(apps, randomPick(orgUnits).id().get()))
//                                .build()))
//                .collect(Collectors.toSet());

//
//        Set<DataFlow> randomFlows = apps.stream()
//                .map(a -> ImmutableDataFlow
//                        .builder()
//                        .source(a.toEntityReference()))
//                .map(b -> b.target(randomAppPick(apps, randomPick(orgUnits).id().get())))
//                .map(b -> b.dataType(randomPick(dataTypes)).build())
//                .collect(Collectors.toSet());
//
//
//        dsl.deleteFrom(DATA_FLOW).execute();
//
//        Set<DataFlow> all = new HashSet<>();
//        all.addAll(randomFlows);
//        all.addAll(expectedFlows);
//        all.addAll(probableFlows);
//
//        dataFlowDao.addFlows(new ArrayList<>(all));

        System.out.println("Done");


    }


    private static EntityReference randomAppPick(List<Application> apps, long orgUnitId) {
        List<Application> appsForOU = apps.stream()
                .filter(a -> a.organisationalUnitId() == orgUnitId)
                .collect(toList());
        return randomPick(appsForOU).toEntityReference();
    }
}
