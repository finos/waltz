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

package com.khartec.waltz.data.database_information;

import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.database_information.DatabaseInformation;
import com.khartec.waltz.model.database_information.DatabaseSummaryStatistics;
import com.khartec.waltz.model.database_information.ImmutableDatabaseInformation;
import com.khartec.waltz.model.database_information.ImmutableDatabaseSummaryStatistics;
import com.khartec.waltz.schema.tables.records.DatabaseInformationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.data.JooqUtilities.mkEndOfLifeStatusDerivedField;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.DatabaseInformation.DATABASE_INFORMATION;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class DatabaseInformationDao {

    private final DSLContext dsl;


    private final static RecordMapper<? super Record, DatabaseInformation> DATABASE_RECORD_MAPPER = r -> {
        DatabaseInformationRecord record = r.into(DATABASE_INFORMATION);
        return ImmutableDatabaseInformation.builder()
                .databaseName(record.getDatabaseName())
                .instanceName(record.getInstanceName())
                .environment(record.getEnvironment())
                .dbmsVendor(record.getDbmsVendor())
                .dbmsName(record.getDbmsName())
                .dbmsVersion(record.getDbmsVersion())
                .externalId(Optional.ofNullable(record.getExternalId()))
                .provenance(record.getProvenance())
                .assetCode(record.getAssetCode())
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
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                .where(APPLICATION.ID.eq(id))
                .fetch(DATABASE_RECORD_MAPPER);
    }


    public Map<Long, List<DatabaseInformation>> findByAppSelector(Select<Record1<Long>> appIdSelector) {
        return dsl
                .select(ENTITY_RELATIONSHIP.ID_A, ENTITY_RELATIONSHIP.KIND_A)
                .select(DATABASE_INFORMATION.fields())
                .from(DATABASE_INFORMATION)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                .where(APPLICATION.ID.in(appIdSelector))
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
                    DATABASE_INFORMATION.ENVIRONMENT.as(environmentInner),
                    DATABASE_INFORMATION.DBMS_VENDOR.as(dbmsVendorInner),
                    DATABASE_INFORMATION.DBMS_NAME,
                    DATABASE_INFORMATION.DBMS_VERSION,
                    mkEndOfLifeStatusDerivedField(DATABASE_INFORMATION.END_OF_LIFE_DATE).as(eolStatusInner))
                .from(DATABASE_INFORMATION)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                .where(APPLICATION.ID.in(appIdSelector))
                .fetch();

        return ImmutableDatabaseSummaryStatistics.builder()
                .vendorCounts(calculateStringTallies(dbInfo, dbmsVendorInner))
                .environmentCounts(calculateStringTallies(dbInfo, environmentInner))
                .endOfLifeStatusCounts(calculateStringTallies(dbInfo, eolStatusInner))
                .build();

    }

}
