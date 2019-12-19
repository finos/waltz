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

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.software_catalog.SoftwarePackageDao;
import com.khartec.waltz.data.software_catalog.SoftwareUsageDao;
import com.khartec.waltz.data.software_catalog.SoftwareVersionDao;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;

@Service
public class SoftwareCatalogService {

    private final ApplicationIdSelectorFactory factory = new ApplicationIdSelectorFactory();
    private final SoftwarePackageDao softwarePackageDao;
    private final SoftwareUsageDao softwareUsageDao;
    private final SoftwareVersionDao softwareVersionDao;


    @Autowired
    public SoftwareCatalogService(SoftwarePackageDao softwarePackageDao,
                                  SoftwareUsageDao softwareUsageDao,
                                  SoftwareVersionDao softwareVersionDao) {
        checkNotNull(softwarePackageDao, "softwarePackageDao cannot be null");
        checkNotNull(softwareUsageDao, "softwareUsageDao cannot be null");
        checkNotNull(softwareVersionDao, "softwareVersionDao cannot be null");

        this.softwarePackageDao = softwarePackageDao;
        this.softwareUsageDao = softwareUsageDao;
        this.softwareVersionDao = softwareVersionDao;
    }


    public SoftwareCatalog makeCatalogForAppIds(List<Long> appIds) {
        List<SoftwareUsage> usages = softwareUsageDao.findByAppIds(appIds);
        Set<Long> packageIds = usages
                .stream()
                .map(SoftwareUsage::softwarePackageId)
                .collect(Collectors.toSet());
        List<SoftwarePackage> packages =
                softwarePackageDao.findByIds(packageIds);

        List<SoftwareVersion> versions = softwareVersionDao.findBySoftwarePackageIds(packageIds);


        return ImmutableSoftwareCatalog.builder()
                .usages(usages)
                .packages(packages)
                .versions(versions)
                .build();
    }


    public SoftwareSummaryStatistics calculateStatisticsForAppIdSelector(IdSelectionOptions options) {

        Select<Record1<Long>> appIdSelector = factory.apply(options);

        List<Tally<String>> vendorCounts = softwarePackageDao.toTallies(appIdSelector, SOFTWARE_PACKAGE.VENDOR);
        List<Tally<String>> groupCounts = softwarePackageDao.toTallies(appIdSelector, SOFTWARE_PACKAGE.GROUP);
        List<Tally<String>> nameCounts = softwarePackageDao.toTallies(appIdSelector, SOFTWARE_PACKAGE.NAME);

        return ImmutableSoftwareSummaryStatistics.builder()
                .vendorCounts(vendorCounts)
                .groupCounts(groupCounts)
                .nameCounts(nameCounts)
                .build();
    }


    public SoftwareCatalog getByPackageId(long id) {
        //todo: (KS) make concurrent
        SoftwarePackage softwarePackage = softwarePackageDao.getById(id);
        List<SoftwareUsage> softwareUsages = softwareUsageDao.findBySoftwarePackageIds(id);
        List<SoftwareVersion> versions = softwareVersionDao.findBySoftwarePackageId(id);

        return ImmutableSoftwareCatalog.builder()
                .usages(softwareUsages)
                .packages(ListUtilities.newArrayList(softwarePackage))
                .versions(versions)
                .build();
    }


    public SoftwareCatalog getByLicenceId(long id) {
        List<SoftwareUsage> softwareUsages = softwareUsageDao.findByLicenceId(id);
        List<Long> packageIds = ListUtilities.map(softwareUsages, su -> su.softwarePackageId());
        List<SoftwarePackage> softwarePackages = softwarePackageDao.findByIds(packageIds);
        List<SoftwareVersion> versions = softwareVersionDao.findByLicenceId(id);

        return ImmutableSoftwareCatalog.builder()
                .usages(softwareUsages)
                .packages(softwarePackages)
                .versions(versions)
                .build();
    }


    public SoftwareCatalog findBySelector(IdSelectionOptions options) {
        //todo: build out selector logic
        switch (options.entityReference().kind()) {
            case SOFTWARE:
                return getByPackageId(options.entityReference().id());
            case LICENCE:
                return getByLicenceId(options.entityReference().id());
            default:
                throw new UnsupportedOperationException("Cannot create Software Catalog from options: " + options);
        }
    }

}
