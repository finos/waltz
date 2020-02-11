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
import com.khartec.waltz.data.authoritative_source.AuthoritativeSourceDao;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthSourceRatingCalculator;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class AuthSourceHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AuthoritativeSourceService svc = ctx.getBean(AuthoritativeSourceService.class);
        AuthSourceRatingCalculator authSourceRatingCalculatorCalculator = ctx.getBean(AuthSourceRatingCalculator.class);
        LogicalFlowDecoratorRatingsCalculator flowCalculator = ctx.getBean(LogicalFlowDecoratorRatingsCalculator.class);
        LogicalFlowDecoratorDao decoratorDao = ctx.getBean(LogicalFlowDecoratorDao.class);
        AuthoritativeSourceDao authoritativeSourceDao = ctx.getBean(AuthoritativeSourceDao.class);


        FunctionUtilities.time("Flow ratings", () -> svc.recalculateAllFlowRatings());

        /*
        List<AuthoritativeRatingVantagePoint> authoritativeRatingVantagePoints = authoritativeSourceDao.findAuthoritativeRatingVantagePoints(SetUtilities.asSet(6645L));
        List<LogicalFlowDecorator> preDecorators = decoratorDao.findByFlowIds(newArrayList(39835L));
        preDecorators.forEach(System.out::println);

        Collection<LogicalFlowDecorator> postDecorators = flowCalculator.calculate(preDecorators);
        System.out.println("---- calc ----");
        postDecorators.forEach(System.out::println);

        Set<LogicalFlowDecorator> modifiedDecorators = SetUtilities.minus(
                fromCollection(postDecorators),
                fromCollection(preDecorators));

        System.out.println("---- mod ----");
        modifiedDecorators.forEach(System.out::println);
        */

        System.exit(-1);
    }



}
