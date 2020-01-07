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
        IdSelectionOptions selectionOptions = IdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 70), HierarchyQueryScope.CHILDREN);

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
