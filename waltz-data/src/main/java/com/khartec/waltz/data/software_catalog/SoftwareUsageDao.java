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

package com.khartec.waltz.data.software_catalog;

import com.khartec.waltz.model.software_catalog.ImmutableSoftwareUsage;
import com.khartec.waltz.model.software_catalog.SoftwareUsage;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
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
        return dsl.selectDistinct(
                SOFTWARE_USAGE.APPLICATION_ID,
                SOFTWARE_USAGE.SOFTWARE_VERSION_ID,
                SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID,
                SOFTWARE_VERSION_LICENCE.LICENCE_ID,
                SOFTWARE_USAGE.PROVENANCE
        )
                .select(SOFTWARE_VERSION.fields())
                .select(SOFTWARE_VERSION_LICENCE.fields())
                .from(SOFTWARE_USAGE)
                .innerJoin(SOFTWARE_VERSION)
                    .on(SOFTWARE_VERSION.ID.eq(SOFTWARE_USAGE.SOFTWARE_VERSION_ID))
                .innerJoin(SOFTWARE_VERSION_LICENCE)
                    .on(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID.eq(SOFTWARE_VERSION.ID))
                .where(condition)
                .fetch(TO_DOMAIN);
    }

}
