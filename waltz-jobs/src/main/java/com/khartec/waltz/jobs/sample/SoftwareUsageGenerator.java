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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.schema.tables.records.SoftwareUsageRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;

public class SoftwareUsageGenerator {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SoftwarePackageDao softwarePackageDao = ctx.getBean(SoftwarePackageDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);
        ApplicationDao applicationDao = ctx.getBean(ApplicationDao.class);

        List<SoftwarePackage> allSoftware = softwarePackageDao.findAll();

        List<SoftwareUsageRecord> records = applicationDao.getAll()
                .stream()
                .flatMap(app -> IntStream.range(0, new Random().nextInt(4) + 1)
                        .mapToObj(x -> new SoftwareUsageRecord(
                                app.id().get(),
                                ListUtilities.randomPick(allSoftware).id().get(),
                                "waltz-random")))
                .collect(Collectors.toList());

        System.out.println("-- deleting all software usages");
        dsl.deleteFrom(SOFTWARE_USAGE)
                .where(SOFTWARE_USAGE.PROVENANCE.eq("waltz-sample"))
                .execute();

        System.out.println(" -- storing usages ( " + records.size() + " )");
        dsl.batchInsert(records).execute();

        System.out.println(" -- done");
    }



}
