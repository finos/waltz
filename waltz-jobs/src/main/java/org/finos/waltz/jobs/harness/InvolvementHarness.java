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

import org.finos.waltz.data.involvement.InvolvementViewDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyDirection;
import org.finos.waltz.model.involvement.InvolvementDetail;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.Map;

import static org.finos.waltz.model.EntityReference.mkRef;


public class InvolvementHarness {


    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        InvolvementViewDao dao = ctx.getBean(InvolvementViewDao.class);

        EntityReference ref = mkRef(EntityKind.MEASURABLE, 80873L);

        findInvolvements(dao, ref);
        dumpInvolvements(dsl, ref);
    }


    private static void dumpInvolvements(DSLContext dsl,
                                         EntityReference ref) {
        String dump = InvolvementViewDao
                .mkInvolvementExtractorQuery(dsl, ref)
                .fetch()
                .format();

        System.out.println(dump);
    }

    private static void findInvolvements(InvolvementViewDao dao,
                                         EntityReference ref) {
        Map<HierarchyDirection, ? extends Collection<InvolvementDetail>> invs = dao.findAllInvolvements(ref);
        System.out.println(invs);
    }


}
