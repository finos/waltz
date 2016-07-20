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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableEntityReference;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.HierarchyQueryScope;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import com.khartec.waltz.model.entity_statistic.EntityStatisticSummary;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_statistic.EntityStatisticService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.EntityStatisticDefinition.ENTITY_STATISTIC_DEFINITION;
import static com.khartec.waltz.schema.tables.EntityStatisticValue.ENTITY_STATISTIC_VALUE;


public class EntityStatisticHarness {

    private static final com.khartec.waltz.schema.tables.EntityStatisticDefinition esd = ENTITY_STATISTIC_DEFINITION.as("esd");
    private static final com.khartec.waltz.schema.tables.EntityStatisticValue esv = ENTITY_STATISTIC_VALUE.as("esv");

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        EntityStatisticValueDao dao = ctx.getBean(EntityStatisticValueDao.class);
        EntityStatisticService service = ctx.getBean(EntityStatisticService.class);


        ApplicationIdSelectionOptions options = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference
                        .builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(40)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();


        List<EntityStatisticSummary> statisticsForAppIdSelector = service.findStatsSummariesForAppIdSelector(options);
        List<EntityStatisticSummary> related = service.findRelatedStatsSummaries(34, options);
//        List<EntityStatisticValue> statisticValuesForAppIdSelector = service.getStatisticValuesForAppIdSelector(6, options);


        System.out.println("done");
    }

}
