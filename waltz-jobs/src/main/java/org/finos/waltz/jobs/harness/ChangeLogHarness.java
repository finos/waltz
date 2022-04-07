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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.FunctionUtilities;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.data.changelog.ChangeLogSummariesDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.service.DIBaseConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Optional;

import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.model.IdSelectionOptions.mkOpts;


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


        System.out.println("done");

    }




}
