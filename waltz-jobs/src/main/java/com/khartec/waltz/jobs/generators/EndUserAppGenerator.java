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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.schema.tables.records.EndUserApplicationRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.common.RandomUtilities.getRandom;
import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

/**
 * Created by dwatkins on 14/12/2015.
 */
public class EndUserAppGenerator implements SampleDataGenerator {


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        OrganisationalUnitDao organisationalUnitDao = ctx.getBean(OrganisationalUnitDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        Set<Long> ids = IdUtilities.toIds(organisationalUnitDao.findAll());

        String[] subjects = {
                "Trade", "Risk" ,"Balance",
                "PnL", "Rate", "Fines",
                "Party", "Confirmations",
                "Settlement", "Instruction",
                "Person", "Profit", "Margin",
                "Finance", "Account"
        };

        String[] types = {
                "Report", "Summary", "Draft",
                "Calculations", "Breaks",
                "Record", "Statement",
                "Information", "Pivot"
        };

        String[] tech = {
                "MS ACCESS",
                "MS EXCEL",
                "VBA"
        };

        final Long[] idCounter = {1L};
        ids.forEach(ouId -> {
            for (int i = 0; i < getRandom().nextInt(3); i++) {
                EndUserApplicationRecord record = dsl.newRecord(END_USER_APPLICATION);
                String name = String.format(
                        "%s %s %s %s",
                        randomPick(subjects),
                        randomPick(subjects),
                        randomPick(types),
                        randomPick(types));

                record.setName(name);
                record.setDescription("About the " + name + " End user app");
                record.setKind(randomPick(tech));
                record.setRiskRating(randomPick(Criticality.values()).name());
                record.setLifecyclePhase(randomPick(LifecyclePhase.values()).name());
                record.setOrganisationalUnitId(ouId);

                record.setId(idCounter[0]++);
                record.insert();
            }
        });

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx).delete(END_USER_APPLICATION).execute();
        return true;
    }
}