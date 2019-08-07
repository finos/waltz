/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.application.ApplicationService;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * Created by dwatkins on 13/05/2016.
 */
public class ApplicationIdSelectorHarness {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();
        DSLContext dsl = ctx.getBean(DSLContext.class);

        ApplicationService applicationService = ctx.getBean(ApplicationService.class);

        ApplicationIdSelectionOptions options = ApplicationIdSelectionOptions.mkOpts(
                EntityReference.mkRef(EntityKind.APP_GROUP, 11309L),
                HierarchyQueryScope.EXACT);


        Select<Record1<Long>> selector = factory.apply(options);
        System.out.println(selector);
        List<Application> apps = applicationService.findByAppIdSelector(options);

        System.out.println("--- sz: "+apps.size());
        apps.forEach(System.out::println);


        System.out.println("--- done");
    }


}
