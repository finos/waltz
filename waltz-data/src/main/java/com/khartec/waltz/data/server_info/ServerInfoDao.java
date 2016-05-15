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

package com.khartec.waltz.data.server_info;

import com.khartec.waltz.model.serverinfo.ImmutableServerInfo;
import com.khartec.waltz.model.serverinfo.ImmutableServerSummaryStatistics;
import com.khartec.waltz.model.serverinfo.ServerInfo;
import com.khartec.waltz.model.serverinfo.ServerSummaryStatistics;
import com.khartec.waltz.model.tally.StringTally;
import com.khartec.waltz.schema.tables.records.ServerInformationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.data.JooqUtilities.calculateTallies;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;


@Repository
public class ServerInfoDao {


    private final DSLContext dsl;


    private final RecordMapper<Record, ServerInfo> recordMapper = r -> {
        ServerInformationRecord row = r.into(ServerInformationRecord.class);
        return ImmutableServerInfo.builder()
                .id(row.getId())
                .assetCode(row.getAssetCode())
                .hostname(row.getHostname())
                .virtual(row.getIsVirtual() == null ? false : row.getIsVirtual())
                .operatingSystem(row.getOperatingSystem())
                .operatingSystemVersion(Optional.ofNullable(row.getOperatingSystemVersion()))
                .environment(row.getEnvironment())
                .location(Optional.ofNullable(row.getLocation()))
                .country(Optional.ofNullable(row.getCountry()))
                .provenance(row.getProvenance())
                .build();
    };


    @Autowired
    public ServerInfoDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl must not be null");
        this.dsl = dsl;
    }


    public List<ServerInfo> findByAssetCode(String assetCode) {
        return dsl.select()
                .from(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.ASSET_CODE.eq(assetCode))
                .fetch(recordMapper);
    }


    public List<ServerInfo> findByAppId(long appId) {
        return dsl.select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .innerJoin(APPLICATION)
                .on(SERVER_INFORMATION.ASSET_CODE.in(APPLICATION.ASSET_CODE, APPLICATION.ASSET_CODE))
                .where(APPLICATION.ID.eq(appId))
                .fetch(recordMapper);
    }


    public int[] bulkSave(List<ServerInfo> servers) {
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
                                    SERVER_INFORMATION.PROVENANCE)
                            .values(
                                    s.hostname(),
                                    s.operatingSystem(),
                                    s.operatingSystemVersion().orElse(""),
                                    s.country().orElse(""),
                                    s.virtual(),
                                    s.environment(),
                                    s.location().orElse(""),
                                    s.assetCode(),
                                    s.provenance()))
                    .collect(Collectors.toList()))
                .execute();
    }


    public ServerSummaryStatistics findStatsForAppSelector(Select<Record1<Long>> appIdSelector) {

        Condition condition = SERVER_INFORMATION.ASSET_CODE
                .in(dsl.select(APPLICATION.ASSET_CODE)
                    .from(APPLICATION)
                    .where(APPLICATION.ID.in(appIdSelector)));

        List<StringTally> environmentTallies = calculateTallies(
                dsl,
                SERVER_INFORMATION,
                SERVER_INFORMATION.ENVIRONMENT,
                condition);

        List<StringTally> operatingSystemTallies = calculateTallies(
                dsl,
                SERVER_INFORMATION,
                SERVER_INFORMATION.OPERATING_SYSTEM,
                condition);

        List<StringTally> locationTallies = calculateTallies(
                dsl,
                SERVER_INFORMATION,
                SERVER_INFORMATION.LOCATION,
                condition);

        Field<BigDecimal> virtualCount = DSL.coalesce(DSL.sum(
                DSL.choose(SERVER_INFORMATION.IS_VIRTUAL)
                        .when(Boolean.TRUE, 1)
                        .otherwise(0)), BigDecimal.ZERO)
                .as("virtual_count");

        Field<BigDecimal> physicalCount = DSL.coalesce(DSL.sum(
                DSL.choose(SERVER_INFORMATION.IS_VIRTUAL)
                        .when(Boolean.TRUE, 0)
                        .otherwise(1)), BigDecimal.ZERO)
                .as("physical_count");

        return dsl.select(
                    virtualCount,
                    physicalCount
                )
                .from(SERVER_INFORMATION)
                .where(condition)
                .fetchOne(r -> ImmutableServerSummaryStatistics.builder()
                        .virtualCount(r.getValue(virtualCount).intValue())
                        .physicalCount(r.getValue(physicalCount).intValue())
                        .operatingSystemCounts(operatingSystemTallies)
                        .environmentCounts(environmentTallies)
                        .locationCounts(locationTallies)
                        .build());
    }


}
