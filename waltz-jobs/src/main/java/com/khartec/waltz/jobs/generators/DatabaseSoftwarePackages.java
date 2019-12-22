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

import com.khartec.waltz.model.software_catalog.ImmutableSoftwarePackage;
import com.khartec.waltz.model.software_catalog.SoftwarePackage;

public interface DatabaseSoftwarePackages {

    String SAMPLE_DATA_PROVENANCE = "waltz-sample";

    SoftwarePackage sqlServer2008 = ImmutableSoftwarePackage.builder()
            .vendor("Microsoft")
            .name("SQL Server")
            //.version("2008")
            .isNotable(true)
            //.maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SAMPLE_DATA_PROVENANCE)
            .description("Microsoft SQL Server is a relational database management system developed by Microsoft. As a database server, it is a software product with the primary function of storing and retrieving data as requested by other software applications which may run either on the same computer or on another computer across a network (including the Internet).")
            .build();

    SoftwarePackage sqlServer2014 = ImmutableSoftwarePackage.copyOf(sqlServer2008);
            //.withVersion("2014")
            //.withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage sqlServer2016 = ImmutableSoftwarePackage.copyOf(sqlServer2008);
            //.withVersion("2016")
            //.withMaturityStatus(MaturityStatus.INVEST);

    SoftwarePackage oracle9 = ImmutableSoftwarePackage.builder()
            .vendor("Oracle")
            .name("Oracle")
            //.version("9")
            .isNotable(true)
            //.maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SAMPLE_DATA_PROVENANCE)
            .description("Oracle Database (commonly referred to as Oracle RDBMS or simply as Oracle) is an object-relational database management system[3] produced and marketed by Oracle Corporation.")
            .build();

    SoftwarePackage oracle10 = ImmutableSoftwarePackage.copyOf(oracle9);
            //.withVersion("10")
            //.withMaturityStatus(MaturityStatus.DISINVEST);

    SoftwarePackage oracle11 = ImmutableSoftwarePackage.copyOf(oracle9);
            //.withVersion("11")
            //.withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage oracle12 = ImmutableSoftwarePackage.copyOf(oracle9);
            //.withVersion("12")
            //.withMaturityStatus(MaturityStatus.INVEST);


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
