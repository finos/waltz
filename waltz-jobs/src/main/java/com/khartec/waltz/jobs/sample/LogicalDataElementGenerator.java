package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.IOUtilities;
import com.khartec.waltz.schema.tables.records.LogicalDataElementRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.LogicalDataElement.LOGICAL_DATA_ELEMENT;

public class LogicalDataElementGenerator {

    private static final Random rnd = new Random();

    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> lines = IOUtilities.readLines(AppGenerator.class.getClassLoader().getResourceAsStream("logical-data-elements.csv"));

        dsl.deleteFrom(LOGICAL_DATA_ELEMENT).execute();

        List<LogicalDataElementRecord> records = lines.stream()
                .skip(1)
                .map(line -> StringUtils.splitPreserveAllTokens(line, ","))
                .filter(cells -> cells.length == 4)
                .map(cells -> {
                    LogicalDataElementRecord record = new LogicalDataElementRecord();
                    record.setExternalId(cells[0]);
                    record.setName(cells[1]);
                    record.setDescription(cells[2]);
                    record.setType(cells[3]);
                    record.setProvenance("waltz");

                    System.out.println(record);
                    return record;
                })
                .collect(Collectors.toList());

        System.out.println("Inserting new LDE's");
        dsl.batchInsert(records).execute();
    }
}
