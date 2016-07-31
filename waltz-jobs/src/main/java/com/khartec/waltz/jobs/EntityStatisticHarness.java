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
import com.khartec.waltz.model.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.entity_statistic.EntityStatisticService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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


        long CTO = 40;
        long OPS = 140;
        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference
                        .builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(OPS)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();


//        ImmediateHierarchy<EntityStatisticSummary> hier = service.findRelatedStatsSummaries(34L, options);
//        List<TallyPack<String>> statTallies = service.findStatTallies(ListUtilities.newArrayList(31L, 34L), options);


        System.out.println("done");
    }

}
