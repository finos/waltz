package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.Severity;
import com.khartec.waltz.schema.tables.records.ChangeLogRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.khartec.waltz.common.ListUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ChangeLog.CHANGE_LOG;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static java.util.stream.Collectors.toSet;

public class ChangeLogGenerator {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

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

        dsl.deleteFrom(CHANGE_LOG).execute();
        dsl.batchInsert(records).execute();

        System.out.println("Inserted " + records.size() + " change log entries");
    }

    private static ChangeLogRecord mkChangeLog(long appId, String email) {
        ChangeLogRecord record = new ChangeLogRecord();
        record.setMessage("Dummy");
        record.setParentId(appId);
        record.setParentKind(EntityKind.APPLICATION.name());
        record.setUserId(email);
        record.setSeverity(Severity.INFORMATION.name());

        return record;
    }
}
