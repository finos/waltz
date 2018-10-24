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

package com.khartec.waltz.data.server_information;

import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.server_information.ImmutableServerInformation;
import com.khartec.waltz.model.server_information.ImmutableServerSummaryStatistics;
import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.model.server_information.ServerSummaryStatistics;
import com.khartec.waltz.schema.tables.records.ServerInformationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.data.JooqUtilities.calculateStringTallies;
import static com.khartec.waltz.data.JooqUtilities.mkEndOfLifeStatusDerivedField;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;
import static java.util.stream.Collectors.counting;
import static org.jooq.impl.DSL.*;


@Repository
public class ServerInformationDao {


    private final DSLContext dsl;


    private final RecordMapper<Record, ServerInformation> recordMapper = r -> {

        ServerInformationRecord row = r.into(ServerInformationRecord.class);

        boolean isVirtual = row.getIsVirtual() == null
                ? false
                : row.getIsVirtual();

        return ImmutableServerInformation.builder()
                .id(row.getId())
                .assetCode(row.getAssetCode())
                .hostname(row.getHostname())
                .virtual(isVirtual)
                .operatingSystem(row.getOperatingSystem())
                .operatingSystemVersion(row.getOperatingSystemVersion())
                .environment(row.getEnvironment())
                .location(row.getLocation())
                .country(row.getCountry())
                .provenance(row.getProvenance())
                .hardwareEndOfLifeDate(row.getHwEndOfLifeDate())
                .operatingSystemEndOfLifeDate(row.getOsEndOfLifeDate())
                .lifecycleStatus(LifecycleStatus.valueOf(row.getLifecycleStatus()))
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
                .where(SERVER_INFORMATION.ASSET_CODE.eq(assetCode))
                .fetch(recordMapper);
    }


    public List<ServerInformation> findByAppId(long appId) {
        return dsl.select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .innerJoin(APPLICATION)
                .on(SERVER_INFORMATION.ASSET_CODE.in(APPLICATION.ASSET_CODE, APPLICATION.ASSET_CODE))
                .where(APPLICATION.ID.eq(appId))
                .fetch(recordMapper);
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
                                    SERVER_INFORMATION.ENVIRONMENT,
                                    SERVER_INFORMATION.LOCATION,
                                    SERVER_INFORMATION.ASSET_CODE,
                                    SERVER_INFORMATION.HW_END_OF_LIFE_DATE,
                                    SERVER_INFORMATION.OS_END_OF_LIFE_DATE,
                                    SERVER_INFORMATION.LIFECYCLE_STATUS,
                                    SERVER_INFORMATION.PROVENANCE)
                            .values(
                                    s.hostname(),
                                    s.operatingSystem(),
                                    s.operatingSystemVersion(),
                                    s.country(),
                                    s.virtual(),
                                    s.environment(),
                                    s.location(),
                                    s.assetCode(),
                                    toSqlDate(s.hardwareEndOfLifeDate()),
                                    toSqlDate(s.operatingSystemEndOfLifeDate()),
                                    s.lifecycleStatus().name(),
                                    s.provenance()))
                    .collect(Collectors.toList()))
                .execute();
    }


    public ServerSummaryStatistics calculateStatsForAppSelector(Select<Record1<Long>> appIdSelector) {

        Field<String> environmentInner = DSL.field("environment_inner", String.class);
        Field<String> operatingSystemInner = DSL.field("operating_system_inner", String.class);
        Field<String> locationInner = DSL.field("location_inner", String.class);
        Field<String> osEolStatusInner = DSL.field("os_eol_status_inner", String.class);
        Field<String> hwEolStatusInner = DSL.field("hw_eol_status_inner", String.class);
        Field<Boolean> isVirtualInner = DSL.field("is_virtual_inner", Boolean.class);

        Condition condition = SERVER_INFORMATION.ASSET_CODE
                .in(select(APPLICATION.ASSET_CODE)
                        .from(APPLICATION)
                        .where(APPLICATION.ID.in(appIdSelector)));

        // de-duplicate host names, as one server can host multiple apps
        Result<? extends Record> serverInfo = dsl.select(
                    max(SERVER_INFORMATION.ENVIRONMENT).as(environmentInner),
                    max(SERVER_INFORMATION.OPERATING_SYSTEM).as(operatingSystemInner),
                    max(SERVER_INFORMATION.LOCATION).as(locationInner),
                    max(mkEndOfLifeStatusDerivedField(SERVER_INFORMATION.OS_END_OF_LIFE_DATE)).as(osEolStatusInner),
                    max(mkEndOfLifeStatusDerivedField(SERVER_INFORMATION.HW_END_OF_LIFE_DATE)).as(hwEolStatusInner),
                    cast(max(when(SERVER_INFORMATION.IS_VIRTUAL.eq(true), 1).otherwise(0)), Boolean.class).as(isVirtualInner))
                .from(SERVER_INFORMATION)
                .where(condition)
                .groupBy(SERVER_INFORMATION.HOSTNAME)
                .fetch();

        Map<Boolean, Long> virtualAndPhysicalCounts = serverInfo.stream()
                .collect(Collectors.groupingBy(r -> r.getValue(isVirtualInner), counting()));

        return ImmutableServerSummaryStatistics.builder()
                .virtualCount(virtualAndPhysicalCounts.getOrDefault(true, 0L))
                .physicalCount(virtualAndPhysicalCounts.getOrDefault(false, 0L))
                .environmentCounts(calculateStringTallies(serverInfo, environmentInner))
                .operatingSystemCounts(calculateStringTallies(serverInfo, operatingSystemInner))
                .locationCounts(calculateStringTallies(serverInfo, locationInner))
                .operatingSystemEndOfLifeStatusCounts(calculateStringTallies(serverInfo, osEolStatusInner))
                .hardwareEndOfLifeStatusCounts(calculateStringTallies(serverInfo, hwEolStatusInner))
                .build();
    }
}
