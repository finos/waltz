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

import com.khartec.waltz.data.scenario.ScenarioAxisItemDao;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.scenario.ScenarioService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.common.ListUtilities.newArrayList;


public class ScenarioHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        ScenarioService scenarioService = ctx.getBean(ScenarioService.class);
        ScenarioAxisItemDao scenarioAxisItemDao = ctx.getBean(ScenarioAxisItemDao.class);

        int[] result = scenarioAxisItemDao
                .reorder(
                    22L,
                    AxisOrientation.ROW,
                    newArrayList(118L, 161L));

        System.out.println(result);

    }

}
