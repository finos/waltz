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

package org.finos.waltz.service.software_catalog;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.application.ApplicationIdSelectorFactory;
import org.finos.waltz.data.software_catalog.SoftwarePackageDao;
import org.finos.waltz.data.software_catalog.SoftwareUsageDao;
import org.finos.waltz.data.software_catalog.SoftwareVersionDao;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.software_catalog.*;
import org.finos.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;
import static org.finos.waltz.common.Checks.checkNotNull;

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


    public SoftwareCatalog getByVersionId(long id) {
        SoftwareVersion version = softwareVersionDao.getByVersionId(id);
        SoftwarePackage softwarePackage = softwarePackageDao.getById(version.softwarePackageId());
        List<SoftwareUsage> softwareUsages = softwareUsageDao.findBySoftwareVersionId(id);

        return ImmutableSoftwareCatalog.builder()
                .usages(softwareUsages)
                .packages(ListUtilities.newArrayList(softwarePackage))
                .versions(ListUtilities.newArrayList(version))
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


    public SoftwareCatalog getBySelector(IdSelectionOptions options) {
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

    public List<SoftwarePackage> search(EntitySearchOptions options) {
        return softwarePackageDao.search(options);
    }
}
