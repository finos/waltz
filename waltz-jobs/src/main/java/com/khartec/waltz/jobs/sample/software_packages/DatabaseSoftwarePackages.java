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

package com.khartec.waltz.jobs.sample.software_packages;

import com.khartec.waltz.jobs.sample.SampleDataUtilities;
import com.khartec.waltz.model.software_catalog.ImmutableSoftwarePackage;
import com.khartec.waltz.model.software_catalog.MaturityStatus;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;

public interface DatabaseSoftwarePackages {

    SoftwarePackage sqlServer2008 = ImmutableSoftwarePackage.builder()
            .vendor("Microsoft")
            .name("SQL Server")
            .version("2008")
            .isNotable(true)
            .maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SampleDataUtilities.SAMPLE_DATA_PROVENANCE)
            .description("Microsoft SQL Server is a relational database management system developed by Microsoft. As a database server, it is a software product with the primary function of storing and retrieving data as requested by other software applications which may run either on the same computer or on another computer across a network (including the Internet).")
            .build();

    SoftwarePackage sqlServer2014 = ImmutableSoftwarePackage.copyOf(sqlServer2008)
            .withVersion("2014")
            .withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage sqlServer2016 = ImmutableSoftwarePackage.copyOf(sqlServer2008)
            .withVersion("2016")
            .withMaturityStatus(MaturityStatus.INVEST);

    SoftwarePackage oracle9 = ImmutableSoftwarePackage.builder()
            .vendor("Oracle")
            .name("Oracle")
            .version("9")
            .isNotable(true)
            .maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SampleDataUtilities.SAMPLE_DATA_PROVENANCE)
            .description("Oracle Database (commonly referred to as Oracle RDBMS or simply as Oracle) is an object-relational database management system[3] produced and marketed by Oracle Corporation.")
            .build();

    SoftwarePackage oracle10 = ImmutableSoftwarePackage.copyOf(oracle9)
            .withVersion("10")
            .withMaturityStatus(MaturityStatus.DISINVEST);

    SoftwarePackage oracle11 = ImmutableSoftwarePackage.copyOf(oracle9)
            .withVersion("11")
            .withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage oracle12 = ImmutableSoftwarePackage.copyOf(oracle9)
            .withVersion("12")
            .withMaturityStatus(MaturityStatus.INVEST);


    SoftwarePackage[] dbs = new SoftwarePackage[] {
            sqlServer2008,
            sqlServer2014,
            sqlServer2016,
            oracle9,
            oracle10,
            oracle11,
            oracle12
    };

}
