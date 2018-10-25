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

import com.khartec.waltz.data.database_information.DatabaseInformationDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.database_information.DatabaseInformationService;
import org.jooq.DSLContext;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class DatabaseHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DatabaseInformationService databaseInfoService = ctx.getBean(DatabaseInformationService.class);
        DatabaseInformationDao databaseDao = ctx.getBean(DatabaseInformationDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        /*
        List<DatabaseInformation> dbs = databaseDao.findByApplicationId(801L);
        System.out.println(dbs.size());


        List<Tally<String>> eolCounts = calculateStringTallies(
                dsl,
                DATABASE_INFORMATION,
                DSL.when(DATABASE_INFORMATION.END_OF_LIFE_DATE.lt(DSL.currentDate()), DSL.inline(EndOfLifeStatus.END_OF_LIFE.name()))
                        .otherwise(DSL.inline(EndOfLifeStatus.NOT_END_OF_LIFE.name())),
                DSL.trueCondition());

        System.out.println(eolCounts);
        */

        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference.builder()
                        .kind(EntityKind.ORG_UNIT)
                        .id(10)
                        .build())
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("stats", () -> databaseInfoService.calculateStatsForAppIdSelector(options));
        }


    }

}
