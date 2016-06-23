package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.service.DIConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Kamran on 22/06/2016.
 */
public class DataGenerator {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        new EntityStatisticGenerator().apply(ctx);
    }
}
