/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.data_flow_decorator.LogicalFlowDecoratorDao;
import com.khartec.waltz.model.data_flow_decorator.LogicalFlowDecorator;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.authoritative_source.AuthSourceRatingCalculator;
import com.khartec.waltz.service.authoritative_source.AuthoritativeSourceService;
import com.khartec.waltz.service.data_flow_decorator.LogicalFlowDecoratorRatingsCalculator;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.SetUtilities.fromCollection;


public class AuthSourceHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        AuthoritativeSourceService svc = ctx.getBean(AuthoritativeSourceService.class);
        AuthSourceRatingCalculator authSourceRatingCalculatorCalculator = ctx.getBean(AuthSourceRatingCalculator.class);
        LogicalFlowDecoratorRatingsCalculator flowCalculator = ctx.getBean(LogicalFlowDecoratorRatingsCalculator.class);
        LogicalFlowDecoratorDao decoratorDao = ctx.getBean(LogicalFlowDecoratorDao.class);


        List<LogicalFlowDecorator> preDecorators = decoratorDao.findByFlowIds(newArrayList(24938L));
        preDecorators.forEach(System.out::println);

        Collection<LogicalFlowDecorator> postDecorators = flowCalculator.calculate(preDecorators);
        System.out.println("---- calc ----");
        postDecorators.forEach(System.out::println);

        Set<LogicalFlowDecorator> modifiedDecorators = SetUtilities.minus(
                fromCollection(postDecorators),
                fromCollection(preDecorators));

        System.out.println("---- mod ----");
        modifiedDecorators.forEach(System.out::println);


        System.exit(-1);
    }



}
