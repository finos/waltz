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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.application.LifecyclePhase;
import com.khartec.waltz.model.utils.IdUtilities;
import com.khartec.waltz.schema.tables.records.EndUserApplicationRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;

/**
 * Created by dwatkins on 14/12/2015.
 */
public class EndUserAppMaker {

    private static final Random rnd = new Random();


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        OrganisationalUnitDao organisationalUnitDao = ctx.getBean(OrganisationalUnitDao.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> ids = IdUtilities.toIds(organisationalUnitDao.findAll());

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

        dsl.delete(END_USER_APPLICATION).execute();
        final Long[] idCounter = {1L};
        ids.forEach(ouId -> {
            for (int i = 0; i < new Random().nextInt(100) + 40; i++) {
                EndUserApplicationRecord record = dsl.newRecord(END_USER_APPLICATION);
                String name = new StringBuilder()
                        .append(randomPick(subjects))
                        .append(" ")
                        .append(randomPick(subjects))
                        .append(" ")
                        .append(randomPick(types))
                        .append(" ")
                        .append(randomPick(types))
                        .toString();

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


    }
}