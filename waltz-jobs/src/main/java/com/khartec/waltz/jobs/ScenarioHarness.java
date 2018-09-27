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

import com.khartec.waltz.model.scenario.CloneScenarioCommand;
import com.khartec.waltz.model.scenario.ImmutableCloneScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.scenario.ScenarioService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class ScenarioHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        ScenarioService scenarioService = ctx.getBean(ScenarioService.class);

        CloneScenarioCommand cloneCommand = ImmutableCloneScenarioCommand.builder()
                .scenarioId(1L)
                .newName("2020 Clone" + System.currentTimeMillis())
                .userId("admin")
                .build();

        Scenario c = scenarioService.cloneScenario(cloneCommand);

        System.out.println(c);




    }

}
