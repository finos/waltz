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

package com.khartec.waltz.service.software_catalog;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.data.software_catalog.SoftwareUsageDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.software_catalog.*;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;
import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;

@Service
public class SoftwareCatalogService {

    private final SoftwarePackageDao softwarePackageDao;
    private final SoftwareUsageDao softwareUsageDao;
    private final ApplicationIdSelectorFactory factory;
    private final DSLContext dsl;


    @Autowired
    public SoftwareCatalogService(SoftwarePackageDao softwarePackageDao,
                                  SoftwareUsageDao softwareUsageDao,
                                  ApplicationIdSelectorFactory factory,
                                  DSLContext dsl) {

        Checks.checkNotNull(softwarePackageDao, "softwarePackageDao cannot be null");
        Checks.checkNotNull(softwareUsageDao, "softwareUsageDao cannot be null");
        Checks.checkNotNull(factory, "factory cannot be null");

        this.softwarePackageDao = softwarePackageDao;
        this.softwareUsageDao = softwareUsageDao;
        this.factory = factory;
        this.dsl = dsl;
    }


    public SoftwareCatalog makeCatalogForAppIds(List<Long> appIds) {
        List<SoftwareUsage> usages = softwareUsageDao.findByAppIds(appIds);
        Set<Long> packageIds = usages.stream()
                .map(u -> u.softwarePackageId())
                .collect(Collectors.toSet());
        List<SoftwarePackage> packages =
                softwarePackageDao.findByIds(packageIds);

        return ImmutableSoftwareCatalog.builder()
                .usages(usages)
                .packages(packages)
                .build();
    }


    private List<Tally<String>> toTallies(Condition condition, Field groupingField) {
        return dsl.select(groupingField, DSL.count(groupingField))
                .from(SOFTWARE_PACKAGE)
                .innerJoin(SOFTWARE_USAGE)
                .on(SOFTWARE_PACKAGE.ID.eq(SOFTWARE_USAGE.SOFTWARE_PACKAGE_ID))
                .where(dsl.renderInlined(condition))
                .groupBy(groupingField)
                .fetch(JooqUtilities.TO_STRING_TALLY);
    }


    public SoftwareSummaryStatistics calculateStatisticsForAppIdSelector(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        Condition condition = SOFTWARE_USAGE.APPLICATION_ID.in(appIdSelector);

        List<Tally<String>> vendorCounts = toTallies(condition, SOFTWARE_PACKAGE.VENDOR);
        List<Tally<String>> maturityCounts = toTallies(condition, SOFTWARE_PACKAGE.MATURITY_STATUS);

        return ImmutableSoftwareSummaryStatistics.builder()
                .vendorCounts(vendorCounts)
                .maturityCounts(maturityCounts)
                .build();
    }

}
