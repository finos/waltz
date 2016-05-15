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

import com.khartec.waltz.data.server_info.ServerInfoDao;
import com.khartec.waltz.model.serverinfo.ServerSummaryStatistics;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.Application.APPLICATION;


public class ServerHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ServerInfoDao serverInfoDao = ctx.getBean(ServerInfoDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        SelectConditionStep<Record1<Long>> idSelector = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(801L, 802L, 803L));

        ServerSummaryStatistics stats = serverInfoDao.findStatsForAppSelector(idSelector);
        System.out.println("stats:"+stats);
    }




}
