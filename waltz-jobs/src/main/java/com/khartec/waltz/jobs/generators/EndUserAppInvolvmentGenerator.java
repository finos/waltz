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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.InvolvementRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.EndUserApplication.END_USER_APPLICATION;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.InvolvementKind.INVOLVEMENT_KIND;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;


public class EndUserAppInvolvmentGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = dsl.select(END_USER_APPLICATION.ID)
                .from(END_USER_APPLICATION)
                .fetch(END_USER_APPLICATION.ID);

        List<String> empIds = dsl.select(PERSON.EMPLOYEE_ID)
                .from(PERSON)
                .innerJoin(PERSON_HIERARCHY)
                .on(PERSON.EMPLOYEE_ID.eq(PERSON_HIERARCHY.EMPLOYEE_ID))
                .where(PERSON_HIERARCHY.LEVEL.lt(4))
                .fetch(PERSON.EMPLOYEE_ID);

        List<Long> invKinds = dsl
                .select(INVOLVEMENT_KIND.ID)
                .from(INVOLVEMENT_KIND)
                .fetch(INVOLVEMENT_KIND.ID);

        List<InvolvementRecord> records = ListUtilities.map(appIds, id -> {
            InvolvementRecord record = dsl.newRecord(INVOLVEMENT);
            record.setProvenance(SAMPLE_DATA_PROVENANCE);
            record.setEmployeeId(randomPick(empIds));
            record.setEntityId(id);
            record.setEntityKind(EntityKind.END_USER_APPLICATION.name());
            record.setKindId(randomPick(invKinds));

            return record;
        });


        log("---saving: "+records.size());
        dsl.batchInsert(records).execute();
        log("---done");

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(INVOLVEMENT)
                .where(INVOLVEMENT.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .and(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.END_USER_APPLICATION.name()))
                .execute();
        return true;
    }
}
