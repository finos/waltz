/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.data.software_catalog.SoftwareUsageDao;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.software_catalog.*;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;

@Service
public class SoftwareCatalogService {

    private final SoftwarePackageDao softwarePackageDao;
    private final SoftwareUsageDao softwareUsageDao;
    private final ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();


    @Autowired
    public SoftwareCatalogService(SoftwarePackageDao softwarePackageDao,
                                  SoftwareUsageDao softwareUsageDao) {

        Checks.checkNotNull(softwarePackageDao, "softwarePackageDao cannot be null");
        Checks.checkNotNull(softwareUsageDao, "softwareUsageDao cannot be null");

        this.softwarePackageDao = softwarePackageDao;
        this.softwareUsageDao = softwareUsageDao;
    }


    public SoftwareCatalog makeCatalogForAppIds(List<Long> appIds) {
        List<SoftwareUsage> usages = softwareUsageDao.findByAppIds(appIds);
        Set<Long> packageIds = usages.stream()
                .map(SoftwareUsage::softwarePackageId)
                .collect(Collectors.toSet());
        List<SoftwarePackage> packages =
                softwarePackageDao.findByIds(packageIds);

        return ImmutableSoftwareCatalog.builder()
                .usages(usages)
                .packages(packages)
                .build();
    }


    public SoftwareSummaryStatistics calculateStatisticsForAppIdSelector(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        List<Tally<String>> vendorCounts = softwarePackageDao.toTallies(appIdSelector, SOFTWARE_PACKAGE.VENDOR);
        List<Tally<String>> maturityCounts = softwarePackageDao.toTallies(appIdSelector, SOFTWARE_PACKAGE.MATURITY_STATUS);

        return ImmutableSoftwareSummaryStatistics.builder()
                .vendorCounts(vendorCounts)
                .maturityCounts(maturityCounts)
                .build();
    }


    public SoftwareCatalog getByPackageId(long id) {

        SoftwarePackage softwarePackage = softwarePackageDao.getById(id);
        List<SoftwareUsage> softwareUsages = softwareUsageDao.findBySoftwarePackageIds(id);

        return ImmutableSoftwareCatalog.builder()
                .usages(softwareUsages)
                .packages(ListUtilities.newArrayList(softwarePackage))
                .build();
    }
}
