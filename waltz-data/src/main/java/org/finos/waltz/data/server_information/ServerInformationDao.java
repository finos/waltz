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

package org.finos.waltz.data.server_information;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.LifecycleStatus;
import org.finos.waltz.model.server_information.ImmutableServerInformation;
import org.finos.waltz.model.server_information.ImmutableServerSummaryBasicStatistics;
import org.finos.waltz.model.server_information.ImmutableServerSummaryStatistics;
import org.finos.waltz.model.server_information.ServerInformation;
import org.finos.waltz.model.server_information.ServerSummaryBasicStatistics;
import org.finos.waltz.model.server_information.ServerSummaryStatistics;
import org.finos.waltz.model.tally.ImmutableTally;
import org.finos.waltz.model.tally.Tally;
import org.finos.waltz.schema.tables.records.ServerInformationRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Record6;
import org.jooq.RecordMapper;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.SelectHavingStep;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.toSqlDate;
import static org.finos.waltz.schema.tables.Application.APPLICATION;
import static org.finos.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;
import static org.finos.waltz.schema.tables.ServerUsage.SERVER_USAGE;
import static org.jooq.impl.DSL.cast;
import static org.jooq.impl.DSL.when;
import static org.jooq.lambda.tuple.Tuple.tuple;


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
        return dsl
                .select()
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
        return dsl
                .select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public ServerInformation getByExternalId(String externalId) {
        return dsl
                .select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public ServerInformation getByHostname(String hostname) {
        return dsl
                .select(SERVER_INFORMATION.fields())
                .from(SERVER_INFORMATION)
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


    public ServerSummaryBasicStatistics calculateBasicStatsForAppSelector(Select<Record1<Long>> appIdSelector) {
        Condition condition = SERVER_USAGE.ENTITY_ID.in(appIdSelector)
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        Map<Boolean, List<Integer>> byVirtualOrNot = dsl
                .selectDistinct(SERVER_INFORMATION.ID, SERVER_INFORMATION.IS_VIRTUAL)
                .from(SERVER_INFORMATION)
                .join(SERVER_USAGE).on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .where(dsl.renderInlined(condition))
                .fetchGroups(SERVER_INFORMATION.IS_VIRTUAL, r -> 1);

        return ImmutableServerSummaryBasicStatistics.builder()
                .physicalCount(byVirtualOrNot.getOrDefault(false, emptyList()).size())
                .virtualCount(byVirtualOrNot.getOrDefault(true, emptyList()).size())
                .build();
    }


    public ServerSummaryStatistics calculateStatsForAppSelector(Select<Record1<Long>> appIdSelector) {

        Field<String> operatingSystemInner = DSL.field("operating_system_inner", String.class);
        Field<String> locationInner = DSL.field("location_inner", String.class);
        Field<String> osEolStatusInner = DSL.field("os_eol_status_inner", String.class);
        Field<String> hwEolStatusInner = DSL.field("hw_eol_status_inner", String.class);
        Field<String> isVirtualInner = DSL.field("is_virtual_inner", String.class);

        Condition condition = SERVER_USAGE.ENTITY_ID.in(appIdSelector)
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

        // de-duplicate host names, as one server can host multiple apps
        SelectConditionStep<Record6<Long, String, String, String, String, String>> qry = DSL
                .selectDistinct(
                    SERVER_INFORMATION.ID,
                    SERVER_INFORMATION.OPERATING_SYSTEM.as(operatingSystemInner),
                    SERVER_INFORMATION.LOCATION.as(locationInner),
                    JooqUtilities.mkEndOfLifeStatusDerivedField(SERVER_INFORMATION.OS_END_OF_LIFE_DATE).as(osEolStatusInner),
                    JooqUtilities.mkEndOfLifeStatusDerivedField(SERVER_INFORMATION.HW_END_OF_LIFE_DATE).as(hwEolStatusInner),
                    cast(when(SERVER_INFORMATION.IS_VIRTUAL.eq(true), "T").otherwise("F"), String.class).as(isVirtualInner))
                .from(SERVER_INFORMATION)
                .join(SERVER_USAGE).on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .where(condition);

        Result<? extends Record> serverInfo =  dsl
                .resultQuery(dsl.renderInlined(qry))
                .fetch();

        // We want to use offsets as the column lookup by field can be considerably slower (+400ms) when volumes are high
        List<Tuple2<Field<String>, Integer>> fieldsToTally = ListUtilities.asList(
                tuple(operatingSystemInner, 1),
                tuple(locationInner, 2),
                tuple(osEolStatusInner, 3),
                tuple(hwEolStatusInner, 4),
                tuple(isVirtualInner, 5));

        Map<Field<String>, Map<String, Long>> rawTallies = prepareRawTallyData(fieldsToTally, serverInfo);

        List<Tally<String>> envStats = calculateEnvironmentStats(appIdSelector);

        return ImmutableServerSummaryStatistics
            .builder()
            .virtualCount(rawTallies.getOrDefault(isVirtualInner, new HashMap<>()).getOrDefault("T", 0L))
            .physicalCount(rawTallies.getOrDefault(isVirtualInner, new HashMap<>()).getOrDefault("F", 0L))
            .environmentCounts(envStats)
            .operatingSystemCounts(toTallies(rawTallies, operatingSystemInner))
            .locationCounts(toTallies(rawTallies, locationInner))
            .operatingSystemEndOfLifeStatusCounts(toTallies(rawTallies, osEolStatusInner))
            .hardwareEndOfLifeStatusCounts(toTallies(rawTallies, hwEolStatusInner))
            .build();
    }


    /**
     * reduces the results to a map, keyed by fields and then counted by value.
     * i.e. `{LOCFIELD -> { "LDN" : 3, "NY": 6 }}`
     *
     * @param fieldsToTally list of tuples describing field/col name and offset [(field, offset),...]
     * @param records  raw info from database
     * @return
     */
    private Map<Field<String>, Map<String, Long>> prepareRawTallyData(
            List<Tuple2<Field<String>, Integer>> fieldsToTally,
            Result<? extends Record> records)
    {
        return records
                .stream()
                .reduce(
                    new HashMap<>(),
                    (acc, r) -> {
                        for(Tuple2<Field<String>, Integer> fieldRef: fieldsToTally) {
                            Map<String, Long> occurrenceCounts = acc.get(fieldRef.v1);
                            if (occurrenceCounts == null) occurrenceCounts = new HashMap<>();
                            String tallyKey = r.get(fieldRef.v2, String.class);
                            Long occurrenceCount = occurrenceCounts.getOrDefault(tallyKey, 0L);
                            occurrenceCounts.put(tallyKey, occurrenceCount + 1);
                            acc.put(fieldRef.v1, occurrenceCounts);
                        }
                        return acc;
                    },
                    (a, b) -> a);
    }


    private List<Tally<String>> toTallies(Map<Field<String>, Map<String, Long>> rawTallies, Field<String> key) {
        return rawTallies
                .getOrDefault(key, new HashMap<>())
                .entrySet()
                .stream()
                .map(e -> ImmutableTally.<String>builder()
                        .id(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }


    private List<Tally<String>> calculateEnvironmentStats(Select<Record1<Long>> appIdSelector) {
        Field<Integer> countField = DSL.count().as("count");

        SelectHavingStep<Record2<String, Integer>> qry = DSL
                .select(SERVER_USAGE.ENVIRONMENT, countField)
                .from(SERVER_USAGE)
                .where(SERVER_USAGE.ENTITY_ID.in(appIdSelector))
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .groupBy(SERVER_USAGE.ENVIRONMENT);

        return dsl
                .resultQuery(dsl.renderInlined(qry))
                .fetch()
                .stream()
                .map(r -> ImmutableTally.<String>builder()
                        .id(r.get(SERVER_USAGE.ENVIRONMENT))
                        .count(r.get(countField, Double.class))
                        .build())
                .collect(Collectors.toList());

    }

}
