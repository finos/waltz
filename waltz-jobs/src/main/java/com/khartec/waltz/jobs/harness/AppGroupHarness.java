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

import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.model.EntityReference.mkRef;


public class AppGroupHarness {

    public static void main(String[] args) {
        System.out.println("--- start");

        ApplicationIdSelectorFactory appSelectorFactory = new ApplicationIdSelectorFactory();

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);

        EntityReference grp5 = mkRef(EntityKind.APP_GROUP, 5);
        Select<Record1<Long>> selector = appSelectorFactory.apply(ApplicationIdSelectionOptions.mkOpts(grp5));

        System.out.println(dsl.render(selector));
        List<Application> apps = applicationDao.findByAppIdSelector(selector);

        System.out.println(apps.size());

        System.out.println("--- done");



    }

}
