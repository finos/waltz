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

import org.finos.waltz.data.logical_flow.LogicalFlowStatsDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.FlowDirection;
import org.finos.waltz.model.logical_flow.FlowInfo;
import org.finos.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;
import java.util.Set;

import static org.finos.waltz.model.EntityReference.mkRef;

public class LogicalFlowStatsHarness {
    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LogicalFlowStatsDao dao = ctx.getBean(LogicalFlowStatsDao.class);

        Map<FlowDirection, Set<FlowInfo>> flowInfoByDirection = dao.getFlowInfoByDirection(mkRef(EntityKind.APPLICATION, 20506L), null);

        System.out.println("--done");

    }
}
