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

package org.finos.waltz.jobs.generators;

import org.finos.waltz.common.ListUtilities;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.data.server_information.ServerInformationDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.LifecycleStatus;
import org.finos.waltz.model.server_information.ImmutableServerInformation;
import org.finos.waltz.model.server_information.ServerInformation;
import org.finos.waltz.schema.tables.records.ServerUsageRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;
import static org.finos.waltz.schema.tables.ServerUsage.SERVER_USAGE;


public class ServerGenerator implements SampleDataGenerator {

    private static final Set<String> commonHostNames = new HashSet<>();

    private static final Random rnd = RandomUtilities.getRandom();


    private static String mkHostName(int i) {
        return randomPick(SampleData.serverPrefixes)
                    + i
                    + randomPick(SampleData.serverPostfixes);
    }


    private static String mkExernalId(int i) {
        return "server-" + i;
    }


    private static List<Long> getServerIds(DSLContext dsl) {
        return dsl.select(SERVER_INFORMATION.ID)
                .from(SERVER_INFORMATION)
                .fetch(SERVER_INFORMATION.ID);
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        ServerInformationDao serverDao = ctx.getBean(ServerInformationDao.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        commonHostNames.clear();

        List<ServerInformation> servers = ListUtilities.newArrayList();

        IntStream.range(0, 10_000)
                .forEach(i -> {
                    String hostName = mkHostName(i);
                    boolean isCommonHost = commonHostNames.contains(hostName);

                    servers.add(
                            ImmutableServerInformation.builder()
                                    .hostname(hostName)
                                    .location(isCommonHost ? SampleData.locations[0] : randomPick(SampleData.locations))
                                    .operatingSystem(isCommonHost ? SampleData.operatingSystems[0] : randomPick(SampleData.operatingSystems))
                                    .operatingSystemVersion(isCommonHost ? SampleData.operatingSystemVersions[0] : randomPick(SampleData.operatingSystemVersions))
                                    .country("UK")
                                    .hardwareEndOfLifeDate(
                                            rnd.nextInt(10) > 5
                                                    ? Date.valueOf(LocalDate.now().plusMonths(rnd.nextInt(12 * 6) - (12 * 3)))
                                                    : null)
                                    .operatingSystemEndOfLifeDate(
                                            rnd.nextInt(10) > 5
                                                    ? Date.valueOf(LocalDate.now().plusMonths(rnd.nextInt(12 * 6) - (12 * 3)))
                                                    : null)
                                    .virtual(isCommonHost || rnd.nextInt(10) > 7)
                                    .provenance(SAMPLE_DATA_PROVENANCE)
                                    .lifecycleStatus(randomPick(LifecycleStatus.values()))
                                    .externalId(mkExernalId(i))
                                    .build());
                });

        serverDao.bulkSave(servers);

        // create server usages
        List<Long> appIds = getAppIds(dsl);
        List<Long> serverIds = getServerIds(dsl);

        HashSet<Tuple2<Long,Long>> serverAppMappings = new HashSet<>();

        IntStream.range(0, 1_000)
                .forEach(i -> {
                    serverAppMappings.add(Tuple.tuple(randomPick(serverIds), randomPick(appIds)));
                });

        List<ServerUsageRecord> serverUsages = serverAppMappings
                .stream()
                .map(t -> mkServerUsageRecord(t.v1, t.v2))
                .collect(Collectors.toList());

        dsl.batchInsert(serverUsages).execute();

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {

        getDsl(ctx)
                .delete(SERVER_INFORMATION)
                .where(SERVER_INFORMATION.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();

        getDsl(ctx)
                .delete(SERVER_USAGE)
                .where(SERVER_USAGE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();

        return true;
    }


    private static ServerUsageRecord mkServerUsageRecord(long serverId, long appId) {
        ServerUsageRecord r = new ServerUsageRecord();
        r.setServerId(serverId);
        r.setEntityKind(EntityKind.APPLICATION.name());
        r.setEntityId(appId);
        r.setEnvironment(randomPick(SampleData.environments));
        r.setLastUpdatedBy("admin");
        r.setProvenance(SAMPLE_DATA_PROVENANCE);
        return r;
    }
}
