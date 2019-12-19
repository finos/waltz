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

package com.khartec.waltz.data.software_catalog;

import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.software_catalog.ImmutableSoftwarePackageView;
import com.khartec.waltz.model.software_catalog.SoftwarePackageView;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.SoftwarePackageRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;
import static com.khartec.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static com.khartec.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;

@Repository
public class SoftwarePackageViewDao {

    private static final Logger LOG = LoggerFactory.getLogger(SoftwarePackageViewDao.class);


    private static final RecordMapper<Record, SoftwarePackageView> TO_VIEW = r -> {
        SoftwarePackageRecord record = r.into(SOFTWARE_PACKAGE);
        return ImmutableSoftwarePackageView.builder()
                .id(record.getId())
                .vendor(record.getVendor())
                .group(record.getGroup())
                .name(record.getName())
                .isNotable(record.getNotable())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .description(record.getDescription())
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .provenance(record.getProvenance())
                .version(r.get(SOFTWARE_VERSION.VERSION))
                .build();
    };


    private final DSLContext dsl;

    @Autowired
    public SoftwarePackageViewDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<SoftwarePackageView> findByIds(Long... ids) {
        return findByCondition(SOFTWARE_PACKAGE.ID.in(ids));
    }

    public List<SoftwarePackageView> findByIds(Collection<Long> ids) {
        return findByCondition(SOFTWARE_PACKAGE.ID.in(ids));
    }


    public List<SoftwarePackageView> findByExternalIds(String... externalIds) {
        return findByCondition(SOFTWARE_PACKAGE.EXTERNAL_ID.in(externalIds));
    }

    public List<SoftwarePackageView> findAll() {
        return findByCondition(DSL.trueCondition());

    }


    // -------------------------
    // ------- PRIVATE ---------
    // -------------------------

    private List<SoftwarePackageView> findByCondition(Condition condition) {
        return dsl.select(SOFTWARE_PACKAGE.fields())
                .select(SOFTWARE_VERSION.fields())
                .from(SOFTWARE_PACKAGE)
                .innerJoin(SOFTWARE_VERSION)
                    .on(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.eq(SOFTWARE_PACKAGE.ID))
                .where(condition)
                .fetch(TO_VIEW);
    }


    public List<Tally<String>> toTallies(Select<Record1<Long>> appIdSelector, Field groupingField) {

        Condition condition = SOFTWARE_USAGE.APPLICATION_ID.in(appIdSelector);

        return dsl
                .select(groupingField, DSL.count(groupingField))
                .from(SOFTWARE_PACKAGE)
                .innerJoin(SOFTWARE_VERSION)
                    .on(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.eq(SOFTWARE_PACKAGE.ID))
                .innerJoin(SOFTWARE_USAGE)
                    .on(SOFTWARE_USAGE.ID.eq(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID))
                .where(dsl.renderInlined(condition))
                .groupBy(groupingField)
                .fetch(JooqUtilities.TO_STRING_TALLY);
    }

}
