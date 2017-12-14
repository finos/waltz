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
        LogicalFlowIdSelectorFactory factory = ctx.getBean(LogicalFlowIdSelectorFactory.class);

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
