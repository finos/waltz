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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


/**
 * Created by dwatkins on 29/09/2016.
 */
public class EndUserAppInvolvmentGenerator {

    private static final Random rnd = new Random();

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> appIds = dsl.select(END_USER_APPLICATION.ID)
                .from(END_USER_APPLICATION)
                .fetch(END_USER_APPLICATION.ID);

        List<String> empIds = dsl.select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .innerJoin(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.LEVEL.lt(4))
                .fetch(PERSON.EMPLOYEE_ID);

        List<InvolvementRecord> records = ListUtilities.map(appIds, id -> {
            InvolvementRecord record = dsl.newRecord(INVOLVEMENT);
            record.setProvenance("RANDOM_GENERATOR");
            record.setEmployeeId(randomPick(empIds));
            record.setEntityId(id);
            record.setEntityKind(EntityKind.END_USER_APPLICATION.name());
            record.setKindId(Long.valueOf(rnd.nextInt(13) + 1));
            return record;
        });


        System.out.println("---saving: "+records.size());
        dsl.batchInsert(records).execute();
        System.out.println("---done");
    }
}
