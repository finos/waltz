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
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.jobs.sample.software_packages.AppServerSoftwarePackages;
import com.khartec.waltz.jobs.sample.software_packages.DatabaseSoftwarePackages;
import com.khartec.waltz.jobs.sample.software_packages.MiddlewareSoftwarePackages;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;

public class SoftwarePackageGenerator {


    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        SoftwarePackageDao softwarePackageDao = ctx.getBean(SoftwarePackageDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        List<SoftwarePackage> softwarePackages = ListUtilities.builder(SoftwarePackage.class)
                .addAll(DatabaseSoftwarePackages.dbs)
                .addAll(AppServerSoftwarePackages.appServers)
                .addAll(MiddlewareSoftwarePackages.middleware)
                .build();


        System.out.println("-- deleting all software packages");
        dsl.deleteFrom(SOFTWARE_PACKAGE)
                .where(SOFTWARE_PACKAGE.PROVENANCE.eq(SampleDataUtilities.SAMPLE_DATA_PROVENANCE))
                .execute();

        System.out.println(" -- storing packages ( " + softwarePackages.size() + " )");
        softwarePackageDao.bulkStore(softwarePackages);

        System.out.println(" -- done");
    }



}
