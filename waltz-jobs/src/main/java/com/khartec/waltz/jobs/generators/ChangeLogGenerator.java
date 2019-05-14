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

import com.khartec.waltz.common.ArrayUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.schema.tables.records.ChangeLogRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.toSet;

public class ChangeLogGenerator implements SampleDataGenerator {

    private static final String[] messages = new String[] {
            "Updated the application",
            "Modified the application",
            "Enriched the application",
            "Added to the application",
            "Removed data from the application"
    };


    private static ChangeLogRecord mkChangeLog(long appId, String email) {
        ChangeLogRecord record = new ChangeLogRecord();
        record.setMessage(randomPick(messages));
        record.setParentId(appId);
        record.setParentKind(EntityKind.APPLICATION.name());
        record.setUserId(email);
        record.setSeverity(Severity.INFORMATION.name());

        return record;
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = ctx.getBean(DSLContext.class);

        // get applications and emails
        List<Long> appIds = dsl.select(APPLICATION.ID)
                .from(APPLICATION)
                .fetch(APPLICATION.ID);

        List<String> emails = dsl.select(PERSON.EMAIL)
                .from(PERSON)
                .fetch(PERSON.EMAIL);

        Set<ChangeLogRecord> records = emails.stream()
                .flatMap(email -> Stream.of(
                        mkChangeLog(randomPick(appIds), email),
                        mkChangeLog(randomPick(appIds), randomPick(emails))))
                .collect(toSet());

        dsl.batchInsert(records).execute();

        log("Inserted " + records.size() + " change log entries");
        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        getDsl(ctx)
                .deleteFrom(CHANGE_LOG)
                .execute();
        return false;
    }
}
