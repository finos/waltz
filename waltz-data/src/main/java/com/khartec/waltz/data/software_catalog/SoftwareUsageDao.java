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
import com.khartec.waltz.schema.tables.records.SoftwareUsageRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;

@Repository
public class SoftwareUsageDao {

    private static final RecordMapper<Record, SoftwareUsage> TO_DOMAIN = r -> {
        SoftwareUsageRecord record = r.into(SOFTWARE_USAGE);
        return ImmutableSoftwareUsage.builder()
                .applicationId(record.getApplicationId())
                .softwarePackageId(record.getSoftwarePackageId())
                .provenance(record.getProvenance())
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
                SOFTWARE_USAGE.SOFTWARE_PACKAGE_ID.in(softwarePackageIds));
    }


    private List<SoftwareUsage> findByCondition(Condition condition) {
        return dsl.select(SOFTWARE_USAGE.fields())
                .from(SOFTWARE_USAGE)
                .where(condition)
                .fetch(TO_DOMAIN);
    }
}
