/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.server_information.ServerInformationDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.LifecycleStatus;
import com.khartec.waltz.model.server_information.ImmutableServerInformation;
import com.khartec.waltz.model.server_information.ServerInformation;
import com.khartec.waltz.schema.tables.records.ServerUsageRecord;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.context.ApplicationContext;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.khartec.waltz.common.ArrayUtilities.randomPick;
import static com.khartec.waltz.schema.tables.ServerInformation.SERVER_INFORMATION;


public class ServerGenerator implements SampleDataGenerator {

    private static final Random rnd = new Random();
    private static final Set<String> commonHostNames = new HashSet<>();


    private static String mkHostName(int i) {
        // have some hosts serving multiple apps
        if (rnd.nextInt(10) > 8) {
            String hostName = SampleData.serverPrefixes[rnd.nextInt(SampleData.serverPrefixes.length - 1)]
                    + "00"
                    + SampleData.serverPostfixes[SampleData.serverPostfixes.length - 1];
            commonHostNames.add(hostName);
            return hostName;
        }

        return randomPick(SampleData.serverPrefixes)
                    + i
                    + randomPick(SampleData.serverPostfixes);
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
                                    .environment(isCommonHost ? SampleData.environments[0] : randomPick(SampleData.environments))
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
                                    .build());
                });

        serverDao.bulkSave(servers);

        // create server usages
        List<Long> appIds = getAppIds(dsl);
        List<Long> serverIds = getServerIds(dsl);

        HashSet<Tuple2<Long,Long>> serverAppMappings = new HashSet<>();

        IntStream.range(0, 20_000)
                .forEach(i -> {
                    serverAppMappings.add(Tuple.tuple(ListUtilities.randomPick(serverIds), ListUtilities.randomPick(appIds)));
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
        return true;
    }


    private static ServerUsageRecord mkServerUsageRecord(long serverId, long appId) {
        ServerUsageRecord r = new ServerUsageRecord();
        r.setServerId(serverId);
        r.setEntityKind(EntityKind.APPLICATION.name());
        r.setEntityId(appId);
        r.setLastUpdatedBy("admin");
        r.setProvenance("waltz");
        return r;
    }
}
