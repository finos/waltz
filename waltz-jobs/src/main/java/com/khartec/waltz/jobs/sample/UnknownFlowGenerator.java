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

import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.schema.tables.records.DataFlowRecord;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.DataFlow.DATA_FLOW;


public class UnknownFlowGenerator {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        ApplicationService applicationDao = ctx.getBean(ApplicationService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Application> allApps = applicationDao.findAll();


        Set<DataFlowRecord> records = IntStream.range(0, 2000)
                .mapToObj(i -> Tuple.tuple(randomPick(allApps).id().get(), randomPick(allApps).id().get()))
                .map(t -> new DataFlowRecord(
                        "APPLICATION",
                        t.v1(),
                        "APPLICATION",
                        t.v2(),
                        "UNKNOWN",
                        "UNK_TEST"))
                .collect(Collectors.toSet());

        dsl.deleteFrom(DATA_FLOW)
                .where(DATA_FLOW.PROVENANCE.eq("UNK_TEST"))
                .execute();

        dsl.batchInsert(records).execute();

        System.out.println("Done");
    }

}
