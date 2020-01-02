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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.data_flow_decorator.DecoratorRatingSummary;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;
import java.util.List;

public class LogicalFlowDecoratorHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        LogicalFlowDecoratorService service = ctx.getBean(LogicalFlowDecoratorService.class);

        List<DecoratorRatingSummary> inboundSummaries = service.summarizeInboundForSelector(IdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 290L), HierarchyQueryScope.CHILDREN));
        inboundSummaries.forEach(s -> System.out.println(String.format("%d %s: %d", s.decoratorEntityReference().id(), s.rating().name(), s.count())));

        System.out.println("--------");

        List<DecoratorRatingSummary> outboundSummaries = service.summarizeOutboundForSelector(IdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 290L), HierarchyQueryScope.CHILDREN));
        outboundSummaries.forEach(s -> System.out.println(String.format("%d %s: %d", s.decoratorEntityReference().id(), s.rating().name(), s.count())));
    }

}
