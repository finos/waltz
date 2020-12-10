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

package com.khartec.waltz.jobs.tools.importers;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.records.ApplicationGroupEntryRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.SetUtilities.minus;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupOuEntry.APPLICATION_GROUP_OU_ENTRY;
import static org.jooq.lambda.tuple.Tuple.tuple;


public class AppGroupFromOrgUnitImporter {

    private static final Logger LOG = LoggerFactory.getLogger(AppGroupFromOrgUnitImporter.class);

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        LOG.debug("Fetching app groups...");
        Result<Record1<Long>> appGroupOrgEntries = fetchAppGroupOUEntries(dsl);

        LOG.debug("Finding applications...");
        Set<Tuple2<Long, Long>> appGroupWithAppId = findAssociatedApplications(dsl, appGroupOrgEntries);

        LOG.debug("Fetching existing app group entry records...");
        Set<Tuple2<Long, Long>> existingAppGroupEntries = findExistingAppGroupEntryRecords(dsl, appGroupOrgEntries);

        LOG.debug("Finding new records...");
        Set<Tuple2<Long, Long>> newRecords = minus(appGroupWithAppId, existingAppGroupEntries);

        LOG.debug("Finding records to remove...");
        Set<Tuple2<Long, Long>> recordsToRemove = minus(existingAppGroupEntries, appGroupWithAppId);

        LOG.debug("Deleting records...");
        int removedRecordCount = dsl.batchDelete(mkRecords(recordsToRemove)).execute().length;
        LOG.debug("Deleted record count: " + removedRecordCount);

        LOG.debug("Inserting records...");
        int insertedRecordCount = dsl.batchInsert(mkRecords(newRecords)).execute().length;
        LOG.debug("Inserted record count: " + insertedRecordCount);

//        System.exit(-1);

    }


    private static Set<Tuple2<Long, Long>> findExistingAppGroupEntryRecords(DSLContext dsl, Result<Record1<Long>> appGroupOrgEntries) {
        return dsl
                .select(APPLICATION_GROUP_ENTRY.GROUP_ID, APPLICATION_GROUP_ENTRY.APPLICATION_ID)
                .from(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.in(appGroupOrgEntries))
                .fetch()
                .stream()
                .map(t -> tuple(t.value1(), t.value2()))
                .collect(Collectors.toSet());
    }


    private static Set<Tuple2<Long, Long>> findAssociatedApplications(DSLContext dsl,
                                                                      Result<Record1<Long>> appGroupOrgEntries) {

        return appGroupOrgEntries
                .stream()
                .flatMap(r ->
                        findAppsPerGroup(dsl, r)
                                .stream()
                                .map(id -> tuple(r.value1(), id)))
                .collect(Collectors.toSet());
    }


    private static List<Long> findAppsPerGroup(DSLContext dsl, Record1<Long> orgUnitId) {

        SelectConditionStep<Record1<Long>> associatedOrgUnits = dsl
                .selectDistinct(ENTITY_HIERARCHY.ID)
                .from(Tables.APPLICATION_GROUP_OU_ENTRY)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID)
                .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())))
                .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(orgUnitId.value1()));

        SelectConditionStep<Record1<Long>> applicationIdsFromAssociatedOrgUnits = dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .where(ORGANISATIONAL_UNIT.ID.in(associatedOrgUnits));

        return dsl
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(applicationIdsFromAssociatedOrgUnits))
                .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name()))
                .fetch()
                .getValues(APPLICATION.ID);
    }


    private static Result<Record1<Long>> fetchAppGroupOUEntries(DSLContext dsl) {
        return dsl
                .selectDistinct(APPLICATION_GROUP_OU_ENTRY.GROUP_ID)
                .from(APPLICATION_GROUP_OU_ENTRY)
                .fetch();
    }


    private static Set<ApplicationGroupEntryRecord> mkRecords(Set<Tuple2<Long, Long>> groupIdToAppIdTuple) {

        return groupIdToAppIdTuple
                .stream()
                .map(t -> {
                    ApplicationGroupEntryRecord record = new ApplicationGroupEntryRecord();
                    record.setGroupId(t.v1);
                    record.setApplicationId(t.v2);

                    return record;

                }).collect(Collectors.toSet());

    };
}