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
