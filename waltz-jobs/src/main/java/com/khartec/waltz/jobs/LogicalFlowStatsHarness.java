/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowStatsDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class LogicalFlowStatsHarness {
    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        LogicalFlowService service = ctx.getBean(LogicalFlowService.class);
        LogicalFlowStatsDao dao = ctx.getBean(LogicalFlowStatsDao.class);
        ApplicationIdSelectorFactory factory = ctx.getBean(ApplicationIdSelectorFactory.class);

        IdSelectionOptions options = IdSelectionOptions.mkOpts(
                EntityReference.mkRef(EntityKind.PERSON, 125613),
                HierarchyQueryScope.CHILDREN);

        Select<Record1<Long>> selector = factory.apply(options);
        System.out.println(selector);

        //
        //tallyDataTypesByAppIdSelector
        //
        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("null check optimisation", () -> {
                dao.tallyDataTypesByAppIdSelector(selector);
                return null;
            });
        }

        //
        // countDistinctFlowInvolvementByAppIdSelector
        //
        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("optimised", () -> {
                return dao.countDistinctFlowInvolvementByAppIdSelector(selector);
            });
        }

        //
        // countDistinctFlowInvolvementByAppIdSelector
        //
        for (int i = 0; i < 5; i++) {
            HarnessUtilities.time("parallel", () -> {
                return dao.countDistinctAppInvolvementByAppIdSelector(selector);
            });
        }

    }
}
