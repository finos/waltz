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

import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
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
