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

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.changelog.ChangeLogSummariesDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIBaseConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class ChangeLogHarness {




    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);

        ChangeLogSummariesDao dao = ctx.getBean(ChangeLogSummariesDao.class);
        EntityReference ref = mkRef(EntityKind.APPLICATION, 1234L);

//        FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
//        FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
//        FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
//        List<ChangeLog> changes = FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
//        System.out.println(changes);

        GenericSelectorFactory factory = new GenericSelectorFactory();

        IdSelectionOptions idSelectionOptions = mkOpts(mkRef(EntityKind.APP_GROUP, 11874), HierarchyQueryScope.EXACT);

        FunctionUtilities.time("test", () -> dao
                .findCountByParentAndChildKindForDateBySelector(
                        factory.applyForKind(EntityKind.APPLICATION, idSelectionOptions),
                        DateTimeUtilities.toSqlDate(DateTimeUtilities.today()),
                        Optional.of(10))
        );

        System.out.println("done");

    }




}
