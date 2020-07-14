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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.tally.DateTally;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.FunctionUtilities.time;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.CHANGE_LOG;


public class ChangeLogSummariesHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ChangeLogService svc = ctx.getBean(ChangeLogService.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        if (false) {
            // enable this if you want to randomize your changelog dates (i.e. for testing)
            dsl.update(CHANGE_LOG)
                    .set(CHANGE_LOG.CREATED_AT,
                            DSL.timestampSub(
                                    DSL.now(),
                                    CHANGE_LOG.ID.plus(CHANGE_LOG.PARENT_ID).mod(360),
                                    DatePart.DAY))
                    .execute();
        }


        IdSelectionOptions opts = mkOpts(mkRef(EntityKind.ORG_UNIT, 20), HierarchyQueryScope.CHILDREN);

        List<DateTally> res = time("findCountByDateForParentKindBySelector", () ->
                svc.findCountByDateForParentKindBySelector(EntityKind.APPLICATION, opts, Optional.empty()));

        System.out.println(res);
    }




}
