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

package com.khartec.waltz.jobs;

import com.khartec.waltz.data.entity_statistic.EntityStatisticValueDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.entity_statistic.RollupKind;
import com.khartec.waltz.model.tally.TallyPack;
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

        /*
        select
  outcome,
  sum(CASE ISNUMERIC(value) when 1 then cast(value as BIGINT) else 0 end)
from entity_statistic_value
where statistic_id = 20010
GROUP BY outcome;
         */

        /*
        AggregateFunction<BigDecimal> summer = DSL.sum(DSL.cast(esv.VALUE, Long.class));

        dsl.select(esv.OUTCOME, summer)
                .from(esv)
                .where(esv.STATISTIC_ID.eq(20010L))
                .groupBy(esv.OUTCOME)
                .fetch()
                .forEach(System.out::println);

*/
        IdSelectionOptions selectionOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 70))
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        // count by value
        List<TallyPack<String>> countByValueTallyPacks = service.calculateHistoricStatTally(10100L, RollupKind.COUNT_BY_ENTITY, selectionOptions, Duration.WEEK);
        System.out.println(countByValueTallyPacks);
        // sum by value
        List<TallyPack<String>> sumByValueTallyPacks = service.calculateHistoricStatTally(20010L, RollupKind.SUM_BY_VALUE, selectionOptions, Duration.YEAR);
        System.out.println(sumByValueTallyPacks);
        // pre-computed
        List<TallyPack<String>> preComputedTallyPacks = service.calculateHistoricStatTally(11000L, RollupKind.NONE, selectionOptions, Duration.ALL);
        System.out.println(preComputedTallyPacks);
        System.out.println("done");
    }

}
