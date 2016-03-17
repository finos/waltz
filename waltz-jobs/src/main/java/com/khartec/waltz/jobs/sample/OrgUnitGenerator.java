package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;

/**
 * Created by dwatkins on 16/03/2016.
 */
public class OrgUnitGenerator {

    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> lines = readLines(OrgUnitGenerator.class.getResourceAsStream("/org-units.csv"));

        System.out.println("Deleting existing OU's");
        dsl.deleteFrom(ORGANISATIONAL_UNIT).execute();

        List<OrganisationalUnitRecord> records = lines.stream()
                .skip(1)
                .map(line -> line.split(","))
                .filter(cells -> cells.length == 5)
                .map(cells -> {
                    OrganisationalUnitRecord record = new OrganisationalUnitRecord();
                    record.setId(longVal(cells[0]));
                    record.setParentId(longVal(cells[1]));
                    record.setName(cells[2]);
                    record.setDescription(cells[3]);
                    record.setKind(cells[4]);
                    record.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

                    System.out.println(record);
                    return record;
                })
                .collect(Collectors.toList());


        System.out.println("Inserting new OU's");
        dsl.batchInsert(records).execute();

        System.out.println("Done");

    }

    private static Long longVal(String value) {
        return StringUtilities.parseLong(value, null);
    }
}
