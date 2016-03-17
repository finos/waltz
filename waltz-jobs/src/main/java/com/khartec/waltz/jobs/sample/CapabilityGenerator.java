/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.sample;

import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.schema.tables.records.CapabilityRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.IOUtilities.readLines;
import static com.khartec.waltz.schema.tables.Capability.CAPABILITY;


public class CapabilityGenerator {

    private static final Random rnd = new Random();

    public static void main(String[] args) throws IOException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<String> lines = readLines(CapabilityGenerator.class.getResourceAsStream("/capabilities.csv"));

        System.out.println("Deleting existing Caps's");
        dsl.deleteFrom(CAPABILITY).execute();

        List<CapabilityRecord> records = lines.stream()
                .skip(1)
                .map(line -> line.split("\t"))
                .filter(cells -> cells.length == 4)
                .map(cells -> {
                    CapabilityRecord record = new CapabilityRecord();
                    record.setId(longVal(cells[0]));
                    record.setParentId(longVal(cells[1]));
                    record.setName(cells[2]);
                    record.setDescription(cells[3]);

                    System.out.println(record);
                    return record;
                })
                .collect(Collectors.toList());


        System.out.println("Inserting new Caps's");
        dsl.batchInsert(records).execute();

        System.out.println("Done");

    }

    private static Long longVal(String value) {
        return StringUtilities.parseLong(value, null);
    }
}
