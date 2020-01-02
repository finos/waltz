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

package com.khartec.waltz.data.server_information;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.server_information.ImmutableServerInformation;
import com.khartec.waltz.model.server_information.ImmutableServerSummaryStatistics;
import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.model.server_information.ServerSummaryStatistics;
import com.khartec.waltz.model.tally.ImmutableTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.ServerInformationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.data.JooqUtilities.mkEndOfLifeStatusDerivedField;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;
import static com.khartec.waltz.schema.tables.ServerUsage.SERVER_USAGE;
import static java.util.stream.Collectors.counting;
import static org.jooq.impl.DSL.cast;
import static org.jooq.impl.DSL.when;


@Repository
public class ServerInformationDao {

    private final DSLContext dsl;


    public static final RecordMapper<Record, ServerInformation> TO_DOMAIN_MAPPER = r -> {

        ServerInformationRecord row = r.into(ServerInformationRecord.class);

        boolean isVirtual = row.getIsVirtual() == null
                ? false
                : row.getIsVirtual();

        return ImmutableServerInformation.builder()
                .id(row.getId())
                .hostname(row.getHostname())
                .virtual(isVirtual)
                .operatingSystem(row.getOperatingSystem())
                .operatingSystemVersion(row.getOperatingSystemVersion())
                .location(row.getLocation())
                .country(row.getCountry())
                .provenance(row.getProvenance())
                .hardwareEndOfLifeDate(row.getHwEndOfLifeDate())
                .operatingSystemEndOfLifeDate(row.getOsEndOfLifeDate())
                .lifecycleStatus(LifecycleStatus.valueOf(row.getLifecycleStatus()))
                .externalId(Optional.ofNullable(row.getExternalId()))
                .build();
    };


    @Autowired
    public ServerInformationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ServerInformation> findByAssetCode(String assetCode) {
        return dsl.select()
                .from(SERVER_INFORMATION)
                .join(SERVER_USAGE)
                    .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                    .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .join(APPLICATION)
                    .on(APPLICATION.ID.eq(SERVER_USAGE.ENTITY_ID))
                .where(APPLICATION.ASSET_CODE.eq(assetCode))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<ServerInformation> findByAppId(long appId) {
        return dsl
                .selectDistinct(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .innerJoin(SERVER_USAGE)
                    .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                    .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .where(SERVER_USAGE.ENTITY_ID.eq(appId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public ServerInformation getById(long id) {
        return dsl.selectFrom(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public ServerInformation getByExternalId(String externalId) {
        return dsl.selectFrom(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public ServerInformation getByHostname(String hostname) {
        return dsl.selectFrom(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.HOSTNAME.eq(hostname))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public int[] bulkSave(List<ServerInformation> servers) {
        return dsl
                .batch(servers.stream()
                    .map(s -> dsl
                            .insertInto(
                                    SERVER_INFORMATION,
                                    SERVER_INFORMATION.HOSTNAME,
                                    SERVER_INFORMATION.OPERATING_SYSTEM,
                                    SERVER_INFORMATION.OPERATING_SYSTEM_VERSION,
                                    SERVER_INFORMATION.COUNTRY,
                                    SERVER_INFORMATION.IS_VIRTUAL,
                                    SERVER_INFORMATION.LOCATION,
                                    SERVER_INFORMATION.HW_END_OF_LIFE_DATE,
                                    SERVER_INFORMATION.OS_END_OF_LIFE_DATE,
                                    SERVER_INFORMATION.LIFECYCLE_STATUS,
                                    SERVER_INFORMATION.PROVENANCE,
                                    SERVER_INFORMATION.EXTERNAL_ID)
                            .values(s.hostname(),
                                    s.operatingSystem(),
                                    s.operatingSystemVersion(),
                                    s.country(),
                                    s.virtual(),
                                    s.location(),
                                    toSqlDate(s.hardwareEndOfLifeDate()),
                                    toSqlDate(s.operatingSystemEndOfLifeDate()),
                                    s.lifecycleStatus().name(),
                                    s.provenance(),
                                    s.externalId().orElse(null)))
                    .collect(Collectors.toList()))
                .execute();
    }


    public ServerSummaryStatistics calculateStatsForAppSelector(Select<Record1<Long>> appIdSelector) {

        Field<String> operatingSystemInner = DSL.field("operating_system_inner", String.class);
        Field<String> locationInner = DSL.field("location_inner", String.class);
        Field<String> osEolStatusInner = DSL.field("os_eol_status_inner", String.class);
        Field<String> hwEolStatusInner = DSL.field("hw_eol_status_inner", String.class);
        Field<Boolean> isVirtualInner = DSL.field("is_virtual_inner", Boolean.class);

        Condition condition = SERVER_USAGE.ENTITY_ID.in(appIdSelector)
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        // de-duplicate host names, as one server can host multiple apps
        SelectConditionStep<Record6<Long, String, String, String, String, Boolean>> qry = dsl.selectDistinct(
                SERVER_INFORMATION.ID,
                SERVER_INFORMATION.OPERATING_SYSTEM.as(operatingSystemInner),
                SERVER_INFORMATION.LOCATION.as(locationInner),
                mkEndOfLifeStatusDerivedField(SERVER_INFORMATION.OS_END_OF_LIFE_DATE).as(osEolStatusInner),
                mkEndOfLifeStatusDerivedField(SERVER_INFORMATION.HW_END_OF_LIFE_DATE).as(hwEolStatusInner),
                cast(when(SERVER_INFORMATION.IS_VIRTUAL.eq(true), 1).otherwise(0), Boolean.class).as(isVirtualInner))
                .from(SERVER_INFORMATION)
                .join(SERVER_USAGE).on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .where(condition);

        Result<? extends Record> serverInfo = qry.fetch();

        Map<Boolean, Long> virtualAndPhysicalCounts = serverInfo.stream()
                .collect(Collectors.groupingBy(r -> r.getValue(isVirtualInner), counting()));

        return ImmutableServerSummaryStatistics.builder()
                .virtualCount(virtualAndPhysicalCounts.getOrDefault(true, 0L))
                .physicalCount(virtualAndPhysicalCounts.getOrDefault(false, 0L))
                .environmentCounts(calculateEnvironmentStats(appIdSelector))
                .operatingSystemCounts(calculateStringTallies(serverInfo, operatingSystemInner))
                .locationCounts(calculateStringTallies(serverInfo, locationInner))
                .operatingSystemEndOfLifeStatusCounts(calculateStringTallies(serverInfo, osEolStatusInner))
                .hardwareEndOfLifeStatusCounts(calculateStringTallies(serverInfo, hwEolStatusInner))
                .build();
    }


    private List<Tally<String>> calculateEnvironmentStats(Select<Record1<Long>> appIdSelector) {
        Field<Integer> countField = DSL.count().as("count");

        return dsl
                .select(SERVER_USAGE.ENVIRONMENT, countField)
                .from(SERVER_USAGE)
                .where(SERVER_USAGE.ENTITY_ID.in(appIdSelector))
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .groupBy(SERVER_USAGE.ENVIRONMENT)
                .fetch()
                .stream()
                .map(r -> ImmutableTally.<String>builder()
                        .id(r.get(SERVER_USAGE.ENVIRONMENT))
                        .count(r.get(countField))
                        .build())
                .collect(Collectors.toList());

    }

}
