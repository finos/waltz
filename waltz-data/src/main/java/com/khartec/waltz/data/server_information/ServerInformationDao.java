/*
 *  This file is part of Waltz.
 *
 *     Waltz is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Waltz is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.server_information;

import com.khartec.waltz.model.server_information.ImmutableServerInformation;
import com.khartec.waltz.model.server_information.ImmutableServerSummaryStatistics;
import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.model.server_information.ServerSummaryStatistics;
import com.khartec.waltz.model.tally.ImmutableStringTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.schema.tables.records.ServerInformationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.data.JooqUtilities.mkEndOfLifeStatusDerivedField;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;
import static java.util.stream.Collectors.*;
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
                                    s.provenance()))
                    .collect(Collectors.toList()))
                .execute();
    }


    public ServerSummaryStatistics findStatsForAppSelector(Select<Record1<Long>> appIdSelector) {

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
        Result<Record6<String, String, String, String, String, Boolean>> serverInfo = dsl.select(
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

        // tally calc function
        Function<Field<String>, List<Tally<String>>> calculateTallies = field -> serverInfo.stream()
                .collect(groupingBy(r -> r.getValue(field), counting()))
                .entrySet()
                .stream()
                .map(e -> ImmutableStringTally.builder()
                        .id(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(toList());

        return ImmutableServerSummaryStatistics.builder()
                .virtualCount(virtualAndPhysicalCounts.get(true))
                .physicalCount(virtualAndPhysicalCounts.get(false))
                .environmentCounts(calculateTallies.apply(environmentInner))
                .operatingSystemCounts(calculateTallies.apply(operatingSystemInner))
                .locationCounts(calculateTallies.apply(locationInner))
                .operatingSystemEndOfLifeStatusCounts(calculateTallies.apply(osEolStatusInner))
                .hardwareEndOfLifeStatusCounts(calculateTallies.apply(hwEolStatusInner))
                .build();
    }
}
