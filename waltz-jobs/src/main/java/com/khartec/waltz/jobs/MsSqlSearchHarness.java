package com.khartec.waltz.jobs;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;

/**
 * Created by dwatkins on 17/03/2016.
 */
public class MsSqlSearchHarness {

    /*
    select name, description
    from capability
    where CONTAINS (*, 'Risk AND Local');

     */

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);



        dsl.select(CAPABILITY.fields())
                .from(CAPABILITY)
                .where(JooqUtilities.MSSQL.mkContains())
                .fetch()
                .forEach(System.out::println);

    }


}
