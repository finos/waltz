package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.schema.tables.records.ProcessRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.TableRecord;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Process.PROCESS;

/**
 * Created by dwatkins on 23/05/2016.
 */
public class ProcessGenerator {

    private static final Random rnd = new Random();


    private static final String[] p1 = new String[] {
            "Client", "Accounting", "Regulatory",
            "Customer", "Financial", "Market",
            "Industry", "Vertical", "Horizontal"
    };

    private static final String[] p2 = new String[] {
            "Onboarding", "Processing", "Reporting",
            "Discovery", "Tracking", "Monitoring",
            ""
    };



    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        Set<TableRecord<?>> records = new HashSet<>();

        for (long g = 0; g < 4; g++ ) {
            ProcessRecord record = new ProcessRecord();
            record.setDescription("Process Group: " + g);
            record.setName("Process Group " + g);
            record.setId(g);
            record.setLevel(1);
            record.setLevel_1(g);
            records.add(record);
            for (long p = 0; p < 10; p++) {
                long id = (g * 100) + p;
                ProcessRecord record2 = new ProcessRecord();
                String name = randomPick(p1)
                        + " "
                        + randomPick(p2);
                record2.setDescription("Process: " + name);
                record2.setName(name);
                record2.setId(id);
                record2.setLevel(2);
                record2.setLevel_1(g);
                record2.setLevel_2(id);
                records.add(record2);

            }
        }

        System.out.println("-- deleting");
        dsl.deleteFrom(PROCESS).execute();
        System.out.println("-- inserting");
        dsl.batchInsert(records).execute();
        System.out.println(" -- done");

    }

}
