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

import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;


public class ChangeInitiativeHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);
        ChangeInitiativeDao dao = ctx.getBean(ChangeInitiativeDao.class);
        ChangeInitiativeIdSelectorFactory selectorFactory = new ChangeInitiativeIdSelectorFactory();

        IdSelectionOptions opts = mkOpts(
                mkRef(EntityKind.APP_GROUP, 2),
                HierarchyQueryScope.EXACT);

        Select<Record1<Long>> selector = selectorFactory.apply(opts);

        dsl.fetch(selector).formatCSV(System.out);

        dao.findForSelector(selector);



    }

}
