/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.schema.tables.records.PhysicalSpecificationRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.schema.tables.PhysicalSpecification.PHYSICAL_SPECIFICATION;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class PhysicalSpecificationGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

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


    private static String mkName(Integer i) {

        return new StringBuilder()
                .append(randomPick(names))
                .append("-")
                .append(randomPick(names))
                .append("-")
                .append(i)
                .append(".")
                .append(randomPick(extensions))
                .toString();
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        List<Long> appIds = getAppIds(dsl);

        List<PhysicalSpecificationRecord> records = appIds
                .stream()
                .flatMap(appId -> IntStream
                        .range(0, rnd.nextInt(4))
                        .mapToObj(i -> tuple(appId, i)))
                .map(t -> {
                    String name = mkName(t.v2);
                    PhysicalSpecificationRecord record = dsl.newRecord(PHYSICAL_SPECIFICATION);
                    record.setOwningEntityId(t.v1);
                    record.setOwningEntityKind(EntityKind.APPLICATION.name());
                    record.setFormat(randomPick(DataFormatKind.values()).name());
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);
                    record.setDescription("Desc "+ name + " " + t.v2);
                    record.setName(name);
                    record.setExternalId("ext-" + t.v1 + "." + t.v2);
                    record.setLastUpdatedBy("admin");
                    return record;
                })
                .collect(Collectors.toList());



        log("---saving: "+records.size());
        dsl.batchInsert(records).execute();
        log("---done");
        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        log("---deleting old demo records");
        dsl.deleteFrom(PHYSICAL_SPECIFICATION)
                .where(PHYSICAL_SPECIFICATION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return false;
    }
}
