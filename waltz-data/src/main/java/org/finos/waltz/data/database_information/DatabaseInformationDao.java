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

package org.finos.waltz.data.database_information;

import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.data.SearchDao;
import org.finos.waltz.data.SearchUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.LifecycleStatus;
import org.finos.waltz.model.database_information.DatabaseInformation;
import org.finos.waltz.model.database_information.DatabaseSummaryStatistics;
import org.finos.waltz.model.database_information.ImmutableDatabaseInformation;
import org.finos.waltz.model.database_information.ImmutableDatabaseSummaryStatistics;
import org.finos.waltz.model.entity_search.EntitySearchOptions;
import org.finos.waltz.schema.tables.records.DatabaseInformationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.schema.Tables.DATABASE_USAGE;
import static org.finos.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;
import static org.finos.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class DatabaseInformationDao implements SearchDao<DatabaseInformation> {

    private final DSLContext dsl;


    public final static RecordMapper<? super Record, DatabaseInformation> DATABASE_RECORD_MAPPER = r -> {
        DatabaseInformationRecord record = r.into(DATABASE_INFORMATION);
        return ImmutableDatabaseInformation.builder()
                .id(record.getId())
                .databaseName(record.getDatabaseName())
                .instanceName(record.getInstanceName())
                .dbmsVendor(record.getDbmsVendor())
                .dbmsName(record.getDbmsName())
                .dbmsVersion(record.getDbmsVersion())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .endOfLifeDate(record.getEndOfLifeDate())
                .lifecycleStatus(LifecycleStatus.valueOf(record.getLifecycleStatus()))
                .build();
    };


    @Autowired
    public DatabaseInformationDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<DatabaseInformation> findByApplicationId(long id) {
        return dsl.select(DATABASE_INFORMATION.fields())
                .from(DATABASE_INFORMATION)
                .join(DATABASE_USAGE)
                .on(DATABASE_USAGE.DATABASE_ID.eq(DATABASE_INFORMATION.ID))
                .and(DATABASE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(DATABASE_USAGE.ENTITY_ID.eq(id))
                .fetch(DATABASE_RECORD_MAPPER);
    }


    public Map<Long, List<DatabaseInformation>> findByAppSelector(Select<Record1<Long>> appIdSelector) {
        return dsl
                .select(ENTITY_RELATIONSHIP.ID_A, ENTITY_RELATIONSHIP.KIND_A)
                .select(DATABASE_INFORMATION.fields())
                .from(DATABASE_INFORMATION)
                .join(DATABASE_USAGE)
                .on(DATABASE_USAGE.DATABASE_ID.eq(DATABASE_INFORMATION.ID))
                .and(DATABASE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(DATABASE_USAGE.ENTITY_ID.in(appIdSelector))
                .fetch()
                .stream()
                .map(r -> tuple(
                        r.getValue(ENTITY_RELATIONSHIP.ID_A),
                        DATABASE_RECORD_MAPPER.map(r)))
                .collect(groupingBy(
                        Tuple2::v1,
                        mapping(Tuple2::v2, Collectors.toList())
                ));
    }


    public DatabaseSummaryStatistics calculateStatsForAppSelector(Select<Record1<Long>> appIdSelector) {

        Field<String> dbmsVendorInner = DSL.field("dbms_vendor_inner", String.class);
        Field<String> environmentInner = DSL.field("environment_inner", String.class);
        Field<String> eolStatusInner = DSL.field("eol_status_inner", String.class);

        // de-duplicate records, as one database can be linked to multiple apps
        Result<? extends Record> dbInfo = dsl.selectDistinct(
                    DATABASE_INFORMATION.DATABASE_NAME,
                    DATABASE_INFORMATION.INSTANCE_NAME,
                    DATABASE_USAGE.ENVIRONMENT.as(environmentInner),
                    DATABASE_INFORMATION.DBMS_VENDOR.as(dbmsVendorInner),
                    DATABASE_INFORMATION.DBMS_NAME,
                    DATABASE_INFORMATION.DBMS_VERSION,
                    JooqUtilities.mkEndOfLifeStatusDerivedField(DATABASE_INFORMATION.END_OF_LIFE_DATE).as(eolStatusInner))
                .from(DATABASE_INFORMATION)
                .join(DATABASE_USAGE)
                .on(DATABASE_USAGE.DATABASE_ID.eq(DATABASE_INFORMATION.ID))
                .and(DATABASE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(DATABASE_USAGE.ENTITY_ID.in(appIdSelector))
                .fetch();

        return ImmutableDatabaseSummaryStatistics.builder()
                .vendorCounts(JooqUtilities.calculateStringTallies(dbInfo, dbmsVendorInner))
                .environmentCounts(JooqUtilities.calculateStringTallies(dbInfo, environmentInner))
                .endOfLifeStatusCounts(JooqUtilities.calculateStringTallies(dbInfo, eolStatusInner))
                .build();

    }

    public DatabaseInformation getById(long id) {
        return dsl.select(DATABASE_INFORMATION.fields())
                .from(DATABASE_INFORMATION)
                .where(DATABASE_INFORMATION.ID.eq(id))
                .fetchOne(DATABASE_RECORD_MAPPER);

    }

    public DatabaseInformation getByExternalId(String externalId) {
        return dsl.select(DATABASE_INFORMATION.fields())
                .from(DATABASE_INFORMATION)
                .where(DATABASE_INFORMATION.EXTERNAL_ID.eq(externalId))
                .fetchOne(DATABASE_RECORD_MAPPER);

    }


    @Override
    public List<DatabaseInformation> search(EntitySearchOptions options) {
            checkNotNull(options, "options cannot be null");
            List<String> terms = SearchUtilities.mkTerms(options.searchQuery());

            if (terms.isEmpty()) {
                return Collections.emptyList();
            }

            Condition externalIdCondition = JooqUtilities.mkStartsWithTermSearch(DATABASE_INFORMATION.EXTERNAL_ID, terms);
            Condition nameCondition = JooqUtilities.mkBasicTermSearch(DATABASE_INFORMATION.DATABASE_NAME, terms);

            return dsl
                    .select(DATABASE_INFORMATION.fields())
                    .from(DATABASE_INFORMATION)
                    .where(externalIdCondition.or(nameCondition))
                    .orderBy(DATABASE_INFORMATION.DATABASE_NAME)
                    .limit(options.limit())
                    .fetch(DATABASE_RECORD_MAPPER);
    }
}
