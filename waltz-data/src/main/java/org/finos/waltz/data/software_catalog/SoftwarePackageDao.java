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
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.model.NameProvider;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.model.software_catalog.ImmutableSoftwarePackage;
import org.finos.waltz.model.software_catalog.SoftwarePackage;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.SetUtilities.orderedUnion;
import static org.finos.waltz.schema.tables.SoftwarePackage.SOFTWARE_PACKAGE;
import static org.finos.waltz.schema.tables.SoftwareUsage.SOFTWARE_USAGE;
import static org.finos.waltz.schema.tables.SoftwareVersion.SOFTWARE_VERSION;

@Repository
public class SoftwarePackageDao implements SearchDao<SoftwarePackage> {

    private static final RecordMapper<Record, SoftwarePackage> TO_DOMAIN = r -> {
        SoftwarePackageRecord record = r.into(SOFTWARE_PACKAGE);
        return ImmutableSoftwarePackage.builder()
                .id(record.getId())
                .vendor(record.getVendor())
                .group(record.getGroup())
                .name(record.getName())
                .isNotable(record.getNotable())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .description(record.getDescription())
                .created(UserTimestamp.mkForUser(record.getCreatedBy(), record.getCreatedAt()))
                .provenance(record.getProvenance())
                .build();
    };


    private final DSLContext dsl;

    @Autowired
    public SoftwarePackageDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<SoftwarePackage> findByIds(Long... ids) {
        return findByCondition(SOFTWARE_PACKAGE.ID.in(ids));
    }


    public List<SoftwarePackage> findByIds(Collection<Long> ids) {
        return findByCondition(SOFTWARE_PACKAGE.ID.in(ids));
    }


    public SoftwarePackage getById(long id) {
        return dsl.select(SOFTWARE_PACKAGE.fields())
                .from(SOFTWARE_PACKAGE)
                .where(SOFTWARE_PACKAGE.ID.eq(id))
                .fetchOne(TO_DOMAIN);
    }

    public List<SoftwarePackage> findAll() {
        return findByCondition(DSL.trueCondition());
    }

    // -----

    private List<SoftwarePackage> findByCondition(Condition condition) {
        return dsl.select(SOFTWARE_PACKAGE.fields())
                .from(SOFTWARE_PACKAGE)
                .where(condition)
                .fetch(TO_DOMAIN);
    }


    public List<Tally<String>> toTallies(Select<Record1<Long>> appIdSelector, Field<String> groupingField) {

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


    @Override
    public List<SoftwarePackage> search(EntitySearchOptions options) {
        checkNotNull(options, "options cannot be null");
        List<String> terms = SearchUtilities.mkTerms(options.searchQuery());

        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        Condition externalIdCondition = JooqUtilities.mkStartsWithTermSearch(SOFTWARE_PACKAGE.EXTERNAL_ID, terms);

        List<SoftwarePackage> serversViaExternalId = dsl
                .select(SOFTWARE_PACKAGE.fields())
                .from(SOFTWARE_PACKAGE)
                .where(externalIdCondition)
                .orderBy(SOFTWARE_PACKAGE.EXTERNAL_ID)
                .limit(options.limit())
                .fetch(SoftwarePackageDao.TO_DOMAIN);

        Condition nameCondition = JooqUtilities.mkBasicTermSearch(SOFTWARE_PACKAGE.NAME, terms);

        List<SoftwarePackage> softwareViaName = dsl
                .select(SOFTWARE_PACKAGE.fields())
                .from(SOFTWARE_PACKAGE)
                .where(nameCondition)
                .orderBy(SOFTWARE_PACKAGE.NAME)
                .limit(options.limit())
                .fetch(SoftwarePackageDao.TO_DOMAIN);

        Condition vendorCondition = JooqUtilities.mkBasicTermSearch(SOFTWARE_PACKAGE.VENDOR, terms);

        List<SoftwarePackage> softwareViaVendor = dsl
                .select(SOFTWARE_PACKAGE.fields())
                .from(SOFTWARE_PACKAGE)
                .where(vendorCondition)
                .orderBy(SOFTWARE_PACKAGE.VENDOR)
                .limit(options.limit())
                .fetch(SoftwarePackageDao.TO_DOMAIN);

        softwareViaName.sort(SearchUtilities.mkRelevancyComparator(NameProvider::name, terms.get(0)));
        softwareViaVendor.sort(SearchUtilities.mkRelevancyComparator(SoftwarePackage::vendor, terms.get(0)));
        serversViaExternalId.sort(SearchUtilities.mkRelevancyComparator(a -> a.externalId().orElse(null), terms.get(0)));

        return new ArrayList<>(orderedUnion(serversViaExternalId, softwareViaName, softwareViaVendor));
    }
}
