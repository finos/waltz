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

import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;


public class DataFlowHarness {


    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LogicalFlowService service = ctx.getBean(LogicalFlowService.class);
        LogicalFlowDao dao = ctx.getBean(LogicalFlowDao.class);
        LogicalFlowIdSelectorFactory factory = new LogicalFlowIdSelectorFactory();

        IdSelectionOptions options = IdSelectionOptions.mkOpts(
                EntityReference.mkRef(EntityKind.ORG_UNIT, 5000),
                HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> selector = factory.apply(options);

        System.out.println(selector);


        List<LogicalFlow> flows = dao.findBySelector(selector);
        flows.forEach(System.out::println);


        // by data type
        EntityReference dataType = EntityReference.mkRef(EntityKind.DATA_TYPE, 6000);
        IdSelectionOptions dataTypeOptions = IdSelectionOptions.mkOpts(dataType, HierarchyQueryScope.CHILDREN);
        List<LogicalFlow> byDataTypeFlows = service.findBySelector(dataTypeOptions);
        byDataTypeFlows.forEach(System.out::println);
        System.out.println(byDataTypeFlows.size());

    }



}
