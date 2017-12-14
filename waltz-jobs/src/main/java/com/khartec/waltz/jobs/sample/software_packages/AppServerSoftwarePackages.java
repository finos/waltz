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

public interface AppServerSoftwarePackages {

    SoftwarePackage tomcat6 = ImmutableSoftwarePackage.builder()
            .vendor("Apache")
            .name("Tomcat")
            .version("6.0")
            .isNotable(true)
            .maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SampleDataUtilities.SAMPLE_DATA_PROVENANCE)
            .description("Apache Tomcat, often referred to as Tomcat, is an open-source web server developed by the Apache Software Foundation (ASF)")
            .build();

    SoftwarePackage tomcat7 = ImmutableSoftwarePackage.copyOf(tomcat6)
            .withVersion("7")
            .withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage tomcat8 = ImmutableSoftwarePackage.copyOf(tomcat6)
            .withVersion("8")
            .withMaturityStatus(MaturityStatus.INVEST);


    SoftwarePackage iis6 = ImmutableSoftwarePackage.builder()
            .vendor("Microsoft")
            .name("IIS")
            .version("6.0")
            .isNotable(true)
            .maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SampleDataUtilities.SAMPLE_DATA_PROVENANCE)
            .description("Internet Information Services (IIS, formerly Internet Information Server) is an extensible web server created by Microsoft for use with Windows NT family.[2] IIS supports HTTP, HTTPS, FTP, FTPS, SMTP and NNTP. It has been an integral part of the Windows NT family since Windows NT 4.0")
            .build();

    SoftwarePackage iis7 = ImmutableSoftwarePackage.copyOf(iis6)
            .withVersion("7")
            .withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage iis75 = ImmutableSoftwarePackage.copyOf(iis6)
            .withVersion("7.5")
            .withMaturityStatus(MaturityStatus.INVEST);


    SoftwarePackage weblogic10 = ImmutableSoftwarePackage.builder()
            .vendor("Oracle")
            .name("Weblogic")
            .version("10.0")
            .isNotable(true)
            .maturityStatus(MaturityStatus.DISINVEST)
            .provenance(SampleDataUtilities.SAMPLE_DATA_PROVENANCE)
            .description("WebLogic is a leading e-commerce online transaction processing (OLTP) platform, developed to connect users in a distributed computing environment and to facilitate the integration of mainframe applications with distributed corporate data and applications.")
            .build();

    SoftwarePackage weblogic11 = ImmutableSoftwarePackage.copyOf(weblogic10)
            .withVersion("11.0")
            .withMaturityStatus(MaturityStatus.HOLD);

    SoftwarePackage weblogic12 = ImmutableSoftwarePackage.copyOf(weblogic10)
            .withVersion("12.0")
            .withMaturityStatus(MaturityStatus.INVEST);


    SoftwarePackage[] appServers = new SoftwarePackage[] {
            tomcat6,
            tomcat7,
            tomcat8,
            iis6,
            iis7,
            iis75,
            weblogic10,
            weblogic11,
            weblogic12
    };

}
