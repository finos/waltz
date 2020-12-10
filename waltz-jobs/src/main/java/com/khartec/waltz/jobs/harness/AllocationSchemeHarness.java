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

import com.khartec.waltz.data.allocation.AllocationDao;
import com.khartec.waltz.data.allocation_scheme.AllocationSchemeDao;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.schema.tables.records.AllocationRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;

public class AllocationSchemeHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        AllocationSchemeDao dao = ctx.getBean(AllocationSchemeDao.class);


        ctx.getBean(DSLContext.class).transaction(tx -> {

            DSLContext dsl = DSL.using(tx);

            AllocationDao allocationDao = new AllocationDao(dsl);


            throw new IllegalArgumentException("Aborting, comment this line if you really mean to execute this removal");
        });

    }
}