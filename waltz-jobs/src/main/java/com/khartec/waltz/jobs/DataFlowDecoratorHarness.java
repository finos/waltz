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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.data_flow_decorator.DataFlowDecoratorService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;

/**
 * Created by dwatkins on 03/09/2016.
 */
public class DataFlowDecoratorHarness {

    public static void main(String[] args) throws SQLException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DataFlowDecoratorService service = ctx.getBean(DataFlowDecoratorService.class);

        EntityReference dataType = EntityReference.mkRef(EntityKind.DATA_TYPE, 8000);
        IdSelectionOptions options = IdSelectionOptions.mkOpts(dataType, HierarchyQueryScope.CHILDREN);

        service.findBySelector(options).forEach(System.out::println);


    }

}
