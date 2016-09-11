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

import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.DataFlow;
import com.khartec.waltz.schema.tables.DataFlowDecorator;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class DataTypeUsageHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DataTypeUsageService service = ctx.getBean(DataTypeUsageService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        DataTypeUsageDao dao = ctx.getBean(DataTypeUsageDao.class);


        /*
select DISTINCT
  df.source_entity_id as app_id,
  dfd.decorator_entity_id as data_type_id,
  app.name as app_name
from data_flow df
INNER JOIN data_flow_decorator dfd on dfd.data_flow_id = df.id
INNER JOIN application app on df.source_entity_id = app.id
where dfd.decorator_entity_id in (8100, 8000)
  and dfd.decorator_entity_kind = 'DATA_TYPE'
  and df.source_entity_kind = 'APPLICATION'
  and df.target_entity_kind = 'APPLICATION'
and source_entity_id not in (
  SELECT target_entity_id
  FROM data_flow df2
    INNER JOIN data_flow_decorator dfd2 ON dfd2.data_flow_id = df2.id
  WHERE dfd2.decorator_entity_id = dfd.decorator_entity_id
)
         */

        DataFlow df = DataFlow.DATA_FLOW.as("df");
        DataFlow df2 = DataFlow.DATA_FLOW.as("df2");
        DataFlowDecorator dfd = DataFlowDecorator.DATA_FLOW_DECORATOR.as("dfd");
        DataFlowDecorator dfd2 = DataFlowDecorator.DATA_FLOW_DECORATOR.as("dfd2");
        com.khartec.waltz.schema.tables.Application app = com.khartec.waltz.schema.tables.Application.APPLICATION.as("app");

        Result<Record3<Long, String, Long>> origins = dsl.selectDistinct(app.ID, app.NAME, dfd.DECORATOR_ENTITY_ID)
                .from(df)
                .innerJoin(dfd).on(dfd.DATA_FLOW_ID.eq(df.ID))
                .innerJoin(app).on(app.ID.eq(df.SOURCE_ENTITY_ID))
                .where(dfd.DECORATOR_ENTITY_ID.in(8000L, 8100L))
                .and(dfd.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                .and(df.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(df.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(df.SOURCE_ENTITY_ID.notIn(
                        DSL.select(df2.TARGET_ENTITY_ID)
                                .from(df2)
                                .innerJoin(dfd2)
                                .on(dfd2.DATA_FLOW_ID.eq(df2.ID))
                                .where(dfd2.DECORATOR_ENTITY_ID.eq(dfd.DECORATOR_ENTITY_ID))
                                .and(dfd2.DECORATOR_ENTITY_KIND.eq(dfd.DECORATOR_ENTITY_KIND))))
                .fetch();

        origins.forEach(System.out::println);
    }


}
