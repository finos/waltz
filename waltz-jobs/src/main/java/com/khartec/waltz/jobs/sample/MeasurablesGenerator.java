package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by dwatkins on 30/12/2016.
 */
public class MeasurablesGenerator {



    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);



    }
}
