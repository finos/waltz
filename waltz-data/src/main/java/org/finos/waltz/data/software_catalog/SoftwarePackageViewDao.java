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

package org.finos.waltz.data.software_catalog;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.software_catalog.ImmutableSoftwarePackageView;
import org.finos.waltz.model.software_catalog.SoftwarePackageView;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.tables.records.SoftwarePackageRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;

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
