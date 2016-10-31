package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.schema.tables.records.PhysicalSpecificationRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * Created by dwatkins on 03/10/2016.
 */
public class PhysicalSpecificationGenerator {

    private static final Random rnd = new Random();



    private static String[] names = {
            "trade",
            "report",
            "risk",
            "ratings",
            "eod",
            "intra-day",
            "yyymmdd",
            "finance",
            "accounting",
            "balance",
            "agg",
            "holdings",
            "accruals",
            "debit",
            "credit",
            "currency",
            "regulatory",
            "transactions",
            "transfers",
            "exchange",
            "summary",
            "daily",
            "position",
            "settlement",
            "confirms",
            "confirmation"
    };


    private static String[] extensions = {
            "xls",
            "txt",
            "csv",
            "tsv",
            "psv",
            "md",
            "bin",
            "xml",
            "json",
            "yaml",
            "yml",
            "pdf",
            "rtf",
            "doc"
    };


    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Long> appIds = dsl.select(APPLICATION.ID)
                .from(APPLICATION)
                .fetch(APPLICATION.ID);

        List<PhysicalSpecificationRecord> records = appIds
                .stream()
                .flatMap(appId -> IntStream
                        .range(0, rnd.nextInt(8))
                        .mapToObj(i -> tuple(appId, i)))
                .map(t -> {
                    String name = mkName();
                    PhysicalSpecificationRecord record = dsl.newRecord(PHYSICAL_SPECIFICATION);
                    record.setOwningEntityId(t.v1);
                    record.setOwningEntityKind(EntityKind.APPLICATION.name());
                    record.setFormat(randomPick(DataFormatKind.values()).name());
                    record.setProvenance("DEMO");
                    record.setDescription("Desc "+ name + " " + t.v2);
                    record.setName(name);
                    record.setExternalId("ext-" + t.v1 + "." + t.v2);
                    return record;
                })
                .collect(Collectors.toList());


        System.out.println("---deleting old demo records");
        dsl.deleteFrom(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.PROVENANCE.eq("DEMO"))
                .execute();
        System.out.println("---saving: "+records.size());
        dsl.batchInsert(records).execute();
        System.out.println("---done");

    }

    private static String mkName() {

        return new StringBuilder()
                .append(randomPick(names))
                .append("-")
                .append(randomPick(names))
                .append(".")
                .append(randomPick(extensions))
                .toString();
    }
}
