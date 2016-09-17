package com.khartec.waltz.jobs;

import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;

/**
 * Created by dwatkins on 17/09/2016.
 */
public class PersonHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        int c = dsl.fetchCount(PERSON, PERSON.MANAGER_EMPLOYEE_ID.eq("").or(PERSON.MANAGER_EMPLOYEE_ID.isNull()));


        int c2 = dsl.fetchCount(DSL.selectDistinct(PERSON_HIERARCHY.MANAGER_ID).from(PERSON_HIERARCHY).where(PERSON_HIERARCHY.LEVEL.eq(1)));
        System.out.println(c);
        System.out.println(c2);

    }
}
