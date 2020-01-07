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

import com.khartec.waltz.data.database_information.DatabaseInformationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
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

        IdSelectionOptions options = IdSelectionOptions.mkOpts(
                EntityReference.mkRef(EntityKind.ORG_UNIT, 10),
                HierarchyQueryScope.CHILDREN);

        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("stats", () -> databaseInfoService.calculateStatsForAppIdSelector(options));
        }


    }

}
