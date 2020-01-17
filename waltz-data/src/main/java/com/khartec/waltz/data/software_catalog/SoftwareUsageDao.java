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

package com.khartec.waltz.data.software_catalog;

import com.khartec.waltz.model.software_catalog.ImmutableSoftwareUsage;
import com.khartec.waltz.model.software_catalog.SoftwareUsage;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static com.khartec.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;
import static com.khartec.waltz.schema.tables.SoftwareVersionLicence.SOFTWARE_VERSION_LICENCE;

@Repository
public class SoftwareUsageDao {

    private static final RecordMapper<Record, SoftwareUsage> TO_DOMAIN = r -> {
        return ImmutableSoftwareUsage.builder()
                .applicationId(r.get(SOFTWARE_USAGE.APPLICATION_ID))
                .softwareVersionId(r.get(SOFTWARE_USAGE.SOFTWARE_VERSION_ID))
                .softwarePackageId(r.get(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID))
                .licenceId(r.get(SOFTWARE_VERSION_LICENCE.LICENCE_ID))
                .provenance(r.get(SOFTWARE_USAGE.PROVENANCE))
                .build();
    };
    

    private final DSLContext dsl;


    @Autowired
    public SoftwareUsageDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<SoftwareUsage> findByAppIds(List<Long> appIds) {
        return findByCondition(
                SOFTWARE_USAGE.APPLICATION_ID.in(appIds));
    }


    public List<SoftwareUsage> findByAppIds(Long... appIds) {
        return findByCondition(
                SOFTWARE_USAGE.APPLICATION_ID.in(appIds));
    }


    public List<SoftwareUsage> findBySoftwarePackageIds(Long... softwarePackageIds) {
        return findByCondition(
                SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.in(softwarePackageIds));
    }


    public List<SoftwareUsage> findBySoftwareVersionId(long versionId) {
        return findByCondition(
                SOFTWARE_VERSION.ID.eq(versionId));
    }


    public List<SoftwareUsage> findByLicenceId(long id) {
        List<SoftwareUsage> byCondition = findByCondition(
                SOFTWARE_VERSION_LICENCE.LICENCE_ID.in(id));
        return byCondition;
    }


    private List<SoftwareUsage> findByCondition(Condition condition) {
        return dsl
            .select(SOFTWARE_USAGE.APPLICATION_ID,
                    SOFTWARE_USAGE.SOFTWARE_VERSION_ID,
                    SOFTWARE_USAGE.PROVENANCE)
            .select(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID)
            .select(SOFTWARE_VERSION_LICENCE.LICENCE_ID)
            .from(SOFTWARE_USAGE)
            .innerJoin(SOFTWARE_VERSION)
            .on(SOFTWARE_VERSION.ID.eq(SOFTWARE_USAGE.SOFTWARE_VERSION_ID))
            .leftJoin(SOFTWARE_VERSION_LICENCE)
            .on(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID.eq(SOFTWARE_VERSION.ID))
            .where(condition)
            .fetch(TO_DOMAIN);
    }

}
