package com.khartec.waltz.jobs;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.Person.PERSON;

public class MsSqlSearchHarness {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);



        dsl.select(PERSON.fields())
                .from(PERSON)
                .where(JooqUtilities.MSSQL.mkContains("fowler"))
                .limit(5)
                .fetch()
                .forEach(p -> System.out.println(p.getValue(PERSON.DISPLAY_NAME)));

    }


}
