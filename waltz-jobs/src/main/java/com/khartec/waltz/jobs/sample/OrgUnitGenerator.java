/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.schema.tables.records.OrganisationalUnitRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.apache.commons.lang3.StringUtils;
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
                .map(line -> StringUtils.splitPreserveAllTokens(line, ","))
                .filter(cells -> cells.length == 4)
                .map(cells -> {
                    OrganisationalUnitRecord record = new OrganisationalUnitRecord();
                    record.setId(longVal(cells[0]));
                    record.setParentId(longVal(cells[1]));
                    record.setName(cells[2]);
                    record.setDescription(cells[3]);
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
