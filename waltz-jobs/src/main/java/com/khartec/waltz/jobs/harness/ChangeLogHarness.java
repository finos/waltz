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

import com.khartec.waltz.common.FunctionUtilities;
import com.khartec.waltz.data.changelog.ChangeLogDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.service.DIBaseConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;


public class ChangeLogHarness {




    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIBaseConfiguration.class);

        ChangeLogDao dao = ctx.getBean(ChangeLogDao.class);
        EntityReference ref = mkRef(EntityKind.APPLICATION, 1234L);

        FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
        FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
        FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
        List<ChangeLog> changes = FunctionUtilities.time("findUnattestedChanges", () -> dao.findUnattestedChanges(ref));
        System.out.println(changes);
    }




}
