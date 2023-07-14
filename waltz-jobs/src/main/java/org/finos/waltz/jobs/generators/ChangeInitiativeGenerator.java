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

import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.common.StreamUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.application.LifecyclePhase;
import org.finos.waltz.model.change_initiative.ChangeInitiativeKind;
import org.finos.waltz.schema.tables.records.ChangeInitiativeRecord;
import org.finos.waltz.schema.tables.records.EntityRelationshipRecord;
import org.finos.waltz.schema.tables.records.InvolvementRecord;
import org.finos.waltz.service.entity_hierarchy.EntityHierarchyService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.TableRecord;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.model.change_initiative.ChangeInitiativeKind.*;
import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.schema.tables.ApplicationGroup.APPLICATION_GROUP;
import static org.finos.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;
import static org.finos.waltz.schema.tables.Involvement.INVOLVEMENT;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class ChangeInitiativeGenerator implements SampleDataGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeInitiativeGenerator.class);
    private static final Random RND = RandomUtilities.getRandom();


    private static final String[] p1 = new String[] {
            "Change", "Enhance", "Deliver", "Adapt to", "Meet",
            "Invest in", "Perform", "Undertake", "Manage",
            "Analyze", "Restructure", "Lead", "Prioritise",
            "Reduce", "Lower"
    };

    private static final String[] p2 = new String[] {
            "Regulatory", "Compliance", "Market",
            "Global", "Regional", "Tactical", "Enterprise",
            "Industry", "Governance", "Auditor",
            "Business", "Customer"
    };

    private static final String[] p3 = new String[] {
            "Processes", "Standards", "Trends",
            "Initiatives", "Reporting", "Operations", "Aggregation",
            "Structures"
    };


    private static Stream<TableRecord<?>> buildPersonLinks(List<Long> ciIds, List<String> employeeIds) {
        return ciIds
                .stream()
                .flatMap(ciId -> IntStream
                    .range(0, 5)
                    .mapToObj(i -> {
                        InvolvementRecord record = new InvolvementRecord();
                        record.setKindId((long) RND.nextInt(13) + 1);
                        record.setProvenance(SAMPLE_DATA_PROVENANCE);
                        record.setEntityId(ciId);
                        record.setEntityKind(EntityKind.CHANGE_INITIATIVE.name());
                        record.setEmployeeId(randomPick(employeeIds));
                        return record;
                    }));
    }


    private static Stream<TableRecord<?>> buildEntityRelationships(EntityKind kind,
                                                                   String relKind,
                                                                   List<Long> ciIds,
                                                                   List<Long> targetIds,
                                                                   double ratioWithCi,
                                                                   int maxLinks) {

        List<Tuple2<Long, Long>> targetAndCiIds = targetIds
                .stream()
                .flatMap(targetId -> RND.nextDouble() <= ratioWithCi
                        ? randomPick(ciIds, RND.nextInt(maxLinks))
                            .stream()
                            .map(ciId -> tuple(targetId, ciId))
                        : Stream.empty())
                .distinct()
                .collect(toList());

        LOG.info("Creating {} records for relationship kind: {}", targetAndCiIds.size(), kind);

        return targetAndCiIds
                .stream()
                .map(t -> {
                    EntityRelationshipRecord record = new EntityRelationshipRecord();

                    record.setKindA(kind.name());
                    record.setIdA(t.v1);

                    record.setKindB(EntityKind.CHANGE_INITIATIVE.name());
                    record.setIdB(t.v2);

                    record.setRelationship(relKind);
                    record.setProvenance(SAMPLE_DATA_PROVENANCE);

                    return record;
                });
    }


    private static ChangeInitiativeRecord buildChangeInitiativeRecord(Tuple4<Long, ChangeInitiativeKind, Long, String> t, List<Long> ouIds) {
        ChangeInitiativeRecord record = new ChangeInitiativeRecord();
        record.setDescription(t.v4);
        record.setName(t.v4);
        record.setProvenance("dummy");
        record.setExternalId("EXT" + t.v1 + (t.v3 != null ? "_" + t.v3 : ""));
        record.setKind(t.v2.name());
        record.setLifecyclePhase(randomPick(LifecyclePhase.values()).name());
        record.setId(t.v1);
        record.setParentId(t.v3);
        record.setStartDate(new Date(Instant.now().toEpochMilli()));
        record.setEndDate(new Date(
                Instant.now()
                    .plusSeconds(RND.nextInt(60 * 60 * 24 * 365 * 2))
                    .toEpochMilli()));
        record.setOrganisationalUnitId(randomPick(ouIds));
        record.setProvenance(SAMPLE_DATA_PROVENANCE);
        return record;

    }

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        DSLContext dsl = getDsl(ctx);
        List<Long> appIds = loadAllIds(dsl, APPLICATION.ID);
        List<Long> ouIds = loadAllIds(dsl, ORGANISATIONAL_UNIT.ID);
        List<Long> groupIds = loadAllIds(dsl, APPLICATION_GROUP.ID);
        List<String> employeeIds = loadAllIds(dsl, PERSON.EMPLOYEE_ID);

        List<ChangeInitiativeRecord> ciRecords = createCiRecords(ouIds);
        dsl.batchInsert(ciRecords).execute();

        LOG.info("Created: {} ci records", ciRecords.size());

        List<Long> ciIds = loadAllIds(dsl, CHANGE_INITIATIVE.ID, CHANGE_INITIATIVE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE));

        List<TableRecord<?>> relationships = StreamUtilities
                .concat(
                        buildPersonLinks(ciIds, employeeIds),
                        buildEntityRelationships(EntityKind.APP_GROUP, "RELATES_TO", ciIds, groupIds, 0.5, 2),
                        buildEntityRelationships(EntityKind.APPLICATION, "SUPPORTS", ciIds, appIds, 0.6, 3))
                .collect(toList());

        LOG.info("Storing {} relationships", relationships.size());
        dsl.batchInsert(relationships).execute();

        EntityHierarchyService ehSvc = ctx.getBean(EntityHierarchyService.class);
        ehSvc.buildFor(EntityKind.CHANGE_INITIATIVE);

        return null;
    }


    private List<ChangeInitiativeRecord> createCiRecords(List<Long> ouIds) {
        AtomicLong idCtr = new AtomicLong();
        return IntStream.range(0, NUM_CHANGE_INITIATIVES)
                .boxed()
                .flatMap(i -> {
                    long initiativeId = idCtr.incrementAndGet();
                    Tuple4<Long, ChangeInitiativeKind, Long, String> initiative = tuple(initiativeId, INITIATIVE, null, mkName());

                    Stream<Tuple4<Long, ChangeInitiativeKind, Long, String>> children = IntStream
                            .range(0, RND.nextInt(4))
                            .boxed()
                            .flatMap(x -> {
                                long programmeId = idCtr.incrementAndGet();
                                Stream<Tuple4<Long, ChangeInitiativeKind, Long, String>> programmes = Stream.of(tuple(programmeId, PROGRAMME, initiativeId, mkName()));
                                Stream<Tuple4<Long, ChangeInitiativeKind, Long, String>> projects = IntStream
                                        .range(0, RND.nextInt(4))
                                        .boxed()
                                        .map(y -> tuple(idCtr.incrementAndGet(), PROJECT, programmeId, mkName()));

                                return Stream.concat(
                                        programmes, projects);
                            });

                    return Stream.concat(Stream.of(initiative), children);
                })
                .map(t -> buildChangeInitiativeRecord(t, ouIds))
                .collect(toList());
    }

    private String mkName() {
        return randomPick(p1)
                                + " "
                                + randomPick(p2)
                                + " "
                                + randomPick(p3);
    }


    @Override
    public boolean remove(ApplicationContext ctx) {
        log("-- deleting");
        SelectConditionStep<Record1<Long>> ciIdsToRemove = DSL
                .select(CHANGE_INITIATIVE.ID)
                .from(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.PROVENANCE.eq(SAMPLE_DATA_PROVENANCE));

        Condition aMatches = ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.CHANGE_INITIATIVE.name())
                .and(ENTITY_RELATIONSHIP.ID_A.in(ciIdsToRemove));

        Condition bMatches = ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.CHANGE_INITIATIVE.name())
                .and(ENTITY_RELATIONSHIP.ID_B.in(ciIdsToRemove));

        getDsl(ctx)
                .deleteFrom(ENTITY_RELATIONSHIP)
                .where(aMatches.or(bMatches))
                .execute();

        getDsl(ctx)
                .deleteFrom(INVOLVEMENT)
                .where(INVOLVEMENT.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name()))
                .and(INVOLVEMENT.ENTITY_ID.in(ciIdsToRemove))
                .execute();

        getDsl(ctx)
                .deleteFrom(CHANGE_INITIATIVE)
                .where(CHANGE_INITIATIVE.ID.in(ciIdsToRemove))
                .execute();

        return true;
    }
}
