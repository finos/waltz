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
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.DSLContext;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;

public class LogicalFlowHarness {
    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LogicalFlowDao dao = ctx.getBean(LogicalFlowDao.class);
        LogicalFlowService service = ctx.getBean(LogicalFlowService.class);
        ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();

        IdSelectionOptions options = IdSelectionOptions.mkOpts(
                mkRef(EntityKind.PERSON, 262508),
                HierarchyQueryScope.CHILDREN);

        for (int i = 0; i < 5; i++) {
            List<LogicalFlow> flows = FunctionUtilities.time("Get flows", () -> service.findBySelector(options));
        }
    }
}
