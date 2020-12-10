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

package com.khartec.waltz.jobs.generators;

import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.physical_flow_participant.ParticipationKind;
import com.khartec.waltz.schema.tables.records.PhysicalFlowParticipantRecord;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static com.khartec.waltz.common.RandomUtilities.randomPick;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static com.khartec.waltz.common.MapUtilities.newHashMap;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlow.PHYSICAL_FLOW;
import static com.khartec.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;
import static com.khartec.waltz.schema.tables.ServerUsage.SERVER_USAGE;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class PhysicalFlowParticipantGenerator implements SampleDataGenerator {

    private static final Random rnd = RandomUtilities.getRandom();

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        log("---creating demo records");

        Map<Criticality, Integer> criticalityCompletionProbabilities = newHashMap(
                Criticality.VERY_HIGH, 80,
                Criticality.HIGH, 70,
                Criticality.MEDIUM, 50,
                Criticality.LOW, 30,
                Criticality.NONE, 10,
                Criticality.UNKNOWN, 10);


        Map<Long, List<Long>> serverIdsByAppId = dsl
                .select(SERVER_USAGE.ENTITY_ID, SERVER_USAGE.SERVER_ID)
                .from(SERVER_USAGE)
                .where(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .fetch()
                .intoGroups(SERVER_USAGE.ENTITY_ID, SERVER_USAGE.SERVER_ID);

        Collection<Long> allServerIds = SetUtilities.unionAll(serverIdsByAppId.values());

        List<PhysicalFlowParticipantRecord> records = dsl
                .select(PHYSICAL_FLOW.ID,
                        PHYSICAL_FLOW.CRITICALITY,
                        LOGICAL_FLOW.SOURCE_ENTITY_ID,
                        LOGICAL_FLOW.TARGET_ENTITY_ID)
                .from(PHYSICAL_FLOW)
                .innerJoin(LOGICAL_FLOW)
                .on(LOGICAL_FLOW.ID.eq(PHYSICAL_FLOW.LOGICAL_FLOW_ID))
                .where(PHYSICAL_FLOW.IS_REMOVED.isFalse())
                .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name()))
                .fetch()
                .stream()
                .map(r -> tuple(
                        r.get(PHYSICAL_FLOW.ID),
                        Criticality.parse(r.get(PHYSICAL_FLOW.CRITICALITY), x -> Criticality.UNKNOWN),
                        r.get(LOGICAL_FLOW.SOURCE_ENTITY_ID),
                        r.get(LOGICAL_FLOW.TARGET_ENTITY_ID)))
                .filter(t -> criticalityCompletionProbabilities.get(t.v2) > rnd.nextInt(100)) // filter based on criticality probability
                .flatMap(t -> Stream.of( // flat map to tuples of (flow_id, source_id/target_id, server_ids, p_kind)
                        tuple(t.v1, t.v3, serverIdsByAppId.getOrDefault(t.v3, emptyList()), ParticipationKind.SOURCE),
                        tuple(t.v1, t.v4, serverIdsByAppId.getOrDefault(t.v4, emptyList()), ParticipationKind.TARGET)))
                .filter(t -> !t.v3.isEmpty()) // no servers therefore filter
                .filter(t -> rnd.nextInt(100) < 80) // even if we have servers some may not be mapped
                .map(t -> t.map3(associatedServerIds -> randomPick(rnd.nextInt(100) < 90
                            ? associatedServerIds // most of the time we'll go with associated servers
                            : allServerIds))) // ... but occasionally we'll go with anything to simulate messy data
                .map(t -> {
                    PhysicalFlowParticipantRecord r = new PhysicalFlowParticipantRecord();
                    r.setPhysicalFlowId(t.v1);
                    r.setParticipantEntityId(t.v3);
                    r.setParticipantEntityKind(EntityKind.SERVER.name());
                    r.setKind(t.v4.name());
                    r.setDescription("Test data");
                    r.setLastUpdatedAt(nowUtcTimestamp());
                    r.setLastUpdatedBy("admin");
                    r.setProvenance(SAMPLE_DATA_PROVENANCE);
                    return r;
                })
                .collect(toList());

        log("About to insert %d records", records.size());
        int[] rcs = dsl.batchInsert(records).execute();
        log("Inserted %d records", rcs.length);

        return null;
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        log("---removing demo records");
        dsl.deleteFrom(PHYSICAL_FLOW_PARTICIPANT)
                .where(PHYSICAL_FLOW_PARTICIPANT.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE))
                .execute();
        return false;
    }

}
