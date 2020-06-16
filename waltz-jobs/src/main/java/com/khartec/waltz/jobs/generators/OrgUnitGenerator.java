/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import com.khartec.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.DSLContext;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;


public class OrgUnitGenerator implements SampleDataGenerator {

    private static Long longVal(String value) {
        return StringUtilities.parseLong(value, null);
    }

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);
        Supplier<List<String>> lineSupplier = Unchecked.supplier(() -> readLines(getClass().getResourceAsStream("/org-units.csv")));


        List<OrganisationalUnitRecord> records = lineSupplier
                .get()
                .stream()
                .skip(1)
                .map(line -> line.split(","))
                .filter(cells -> cells.length > 2)
                .map(cells -> {
                    OrganisationalUnitRecord record = new OrganisationalUnitRecord();
                    record.setId(longVal(cells[0]));
                    record.setParentId(longVal(cells[1]));
                    record.setName(cells[2]);
                    if (cells.length > 3) {
                        record.setDescription(cells[3]);
                    }
                    record.setLastUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    return record;
                })
                .collect(Collectors.toList());


        log("Inserting new OU's");
        dsl.batchInsert(records).execute();

        EntityHierarchyService ehSvc = ctx.getBean(EntityHierarchyService.class);
        ehSvc.buildFor(EntityKind.ORG_UNIT);

        return MapUtilities.newHashMap("created", records.size());
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        log("Deleting existing OU's");
        DSLContext dsl = getDsl(ctx);
        dsl.deleteFrom(ORGANISATIONAL_UNIT).execute();
        return true;
    }

}
