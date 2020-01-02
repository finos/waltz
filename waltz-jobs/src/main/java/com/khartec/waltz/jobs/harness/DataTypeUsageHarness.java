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

import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.data_type_usage.DataTypeUsage;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.usage_info.DataTypeUsageService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class DataTypeUsageHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        DataTypeUsageDao dao = ctx.getBean(DataTypeUsageDao.class);
        DataTypeUsageService svc = ctx.getBean(DataTypeUsageService.class);


        long st = System.currentTimeMillis();

        dao.recalculateForAllApplications();
        List<DataTypeUsage> dtUsages = svc.findForDataTypeSelector(mkOpts(mkRef(EntityKind.DATA_TYPE, 3000), HierarchyQueryScope.CHILDREN));
        System.out.println("Data Type usages: " + dtUsages.size());

        List<DataTypeUsage> actorUsages = svc.findForEntity(mkRef(EntityKind.ACTOR, 16L));
        System.out.println("Actor usages: " + actorUsages.size());
        System.out.println("Took "+ (System.currentTimeMillis() - st));
    }


}
