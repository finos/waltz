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

package org.finos.waltz.data.custom_environment;

import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.records.CustomEnvironmentUsageRecord;
import org.finos.waltz.data.application.ApplicationDao;
import org.finos.waltz.data.database_information.DatabaseInformationDao;
import org.finos.waltz.data.server_information.ServerInformationDao;
import org.finos.waltz.model.CustomEnvironmentAsset;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.custom_environment.CustomEnvironmentUsage;
import org.finos.waltz.model.custom_environment.CustomEnvironmentUsageInfo;
import org.finos.waltz.model.custom_environment.ImmutableCustomEnvironmentUsage;
import org.finos.waltz.model.custom_environment.ImmutableCustomEnvironmentUsageInfo;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectOnConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static org.finos.waltz.schema.Tables.*;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.model.EntityKind.valueOf;
import static org.finos.waltz.model.EntityReference.mkRef;


@Repository
public class CustomEnvironmentUsageDao {

    private final DSLContext dsl;

    private static final Application DATABASE_OWNING_APP = APPLICATION.as("database_owning_app");
    private static final Application SERVER_OWNING_APP = APPLICATION.as("server_owning_app");

    private static final RecordMapper<Record, CustomEnvironmentUsage> TO_CUSTOM_ENV_USAGE_MAPPER = r -> {
        CustomEnvironmentUsageRecord record = r.into(CUSTOM_ENVIRONMENT_USAGE);

        EntityReference ref = mkRef(valueOf(record.getEntityKind()), record.getEntityId());

        return ImmutableCustomEnvironmentUsage.builder()
                .id(record.getId())
                .customEnvironmentId(record.getCustomEnvironmentId())
                .entityReference(ref)
                .createdAt(toLocalDateTime(record.getCreatedAt()))
                .createdBy(record.getCreatedBy())
                .provenance(record.getProvenance())
                .build();
    };


    private static final RecordMapper<Record, CustomEnvironmentUsageInfo> TO_USAGE_INFO_MAPPER = r -> {
        CustomEnvironmentUsageRecord record = r.into(CUSTOM_ENVIRONMENT_USAGE);

        EntityReference ref = mkRef(valueOf(record.getEntityKind()), record.getEntityId());

        CustomEnvironmentUsage usage = ImmutableCustomEnvironmentUsage.builder()
                .id(record.getId())
                .customEnvironmentId(record.getCustomEnvironmentId())
                .entityReference(ref)
                .createdAt(toLocalDateTime(record.getCreatedAt()))
                .createdBy(record.getCreatedBy())
                .provenance(record.getProvenance())
                .build();

        ImmutableCustomEnvironmentUsageInfo.Builder<CustomEnvironmentAsset> infoBuilder = ImmutableCustomEnvironmentUsageInfo.builder()
                .usage(usage);

        if (r.get(SERVER_INFORMATION.ID) != null){

            infoBuilder
                    .owningApplication(ApplicationDao.TO_DOMAIN_MAPPER
                            .map(r.into(SERVER_OWNING_APP)));

            infoBuilder.asset(ServerInformationDao.TO_DOMAIN_MAPPER.map(r));

        } else if (r.get(DATABASE_INFORMATION.ID) != null) {

            infoBuilder
                    .owningApplication(ApplicationDao.TO_DOMAIN_MAPPER
                            .map(r.into(DATABASE_OWNING_APP)));

            infoBuilder.asset(DatabaseInformationDao.DATABASE_RECORD_MAPPER.map(r));
        }


        return infoBuilder.build();
    };


    @Autowired
    public CustomEnvironmentUsageDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<CustomEnvironmentUsage> findByOwningEntityRef(EntityReference ref){
        return dsl
                .select(CUSTOM_ENVIRONMENT_USAGE.fields())
                .from(CUSTOM_ENVIRONMENT_USAGE)
                .innerJoin(CUSTOM_ENVIRONMENT)
                .on(CUSTOM_ENVIRONMENT_USAGE.CUSTOM_ENVIRONMENT_ID.eq(CUSTOM_ENVIRONMENT.ID))
                .where(CUSTOM_ENVIRONMENT.OWNING_ENTITY_ID.eq(ref.id())
                        .and(CUSTOM_ENVIRONMENT.OWNING_ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_CUSTOM_ENV_USAGE_MAPPER);
    }


    public boolean remove(Long usageId){
        return dsl
                .deleteFrom(CUSTOM_ENVIRONMENT_USAGE)
                .where(CUSTOM_ENVIRONMENT_USAGE.ID.eq(usageId))
                .execute() == 1;
    }


    public Long addAsset(CustomEnvironmentUsage usage, String username){
        return dsl
                .insertInto(CUSTOM_ENVIRONMENT_USAGE)
                .set(CUSTOM_ENVIRONMENT_USAGE.CUSTOM_ENVIRONMENT_ID, usage.customEnvironmentId())
                .set(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID, usage.entityReference().id())
                .set(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND, usage.entityReference().kind().name())
                .set(CUSTOM_ENVIRONMENT_USAGE.CREATED_AT, nowUtcTimestamp())
                .set(CUSTOM_ENVIRONMENT_USAGE.CREATED_BY, username)
                .set(CUSTOM_ENVIRONMENT_USAGE.PROVENANCE, usage.provenance())
                .returning(CUSTOM_ENVIRONMENT_USAGE.ID)
                .fetchOne()
                .getId();
    }


    public Set<CustomEnvironmentUsage> findByEntityRef(EntityReference ref){
        return dsl
                .select(CUSTOM_ENVIRONMENT_USAGE.fields())
                .from(CUSTOM_ENVIRONMENT_USAGE)
                .where(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(ref.id())
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_CUSTOM_ENV_USAGE_MAPPER);
    }


    public Set<CustomEnvironmentUsageInfo> findUsageInfoByOwningRef(EntityReference ref){

        return fetchUsageInfoQuery()
                .where(CUSTOM_ENVIRONMENT.OWNING_ENTITY_ID.eq(ref.id())
                        .and(CUSTOM_ENVIRONMENT.OWNING_ENTITY_KIND.eq(ref.kind().name())))
                .and(DATABASE_INFORMATION.ID.isNotNull()
                        .or(SERVER_INFORMATION.ID.isNotNull()))
                .fetchSet(TO_USAGE_INFO_MAPPER);
    }


    public CustomEnvironmentUsageInfo getUsageInfoById(Long usageId){

        return fetchUsageInfoQuery()
                .where(CUSTOM_ENVIRONMENT_USAGE.ID.eq(usageId))
                .and(DATABASE_INFORMATION.ID.isNotNull()
                        .or(SERVER_INFORMATION.ID.isNotNull()))
                .fetchOne(TO_USAGE_INFO_MAPPER);
    }


    private SelectOnConditionStep<Record> fetchUsageInfoQuery() {
        return dsl
                .select(CUSTOM_ENVIRONMENT_USAGE.fields())
                .select(SERVER_INFORMATION.fields())
                .select(SERVER_OWNING_APP.fields())
                .select(DATABASE_INFORMATION.fields())
                .select(DATABASE_OWNING_APP.fields())
                .from(CUSTOM_ENVIRONMENT_USAGE)
                .innerJoin(CUSTOM_ENVIRONMENT)
                .on(CUSTOM_ENVIRONMENT_USAGE.CUSTOM_ENVIRONMENT_ID.eq(CUSTOM_ENVIRONMENT.ID))
                .leftJoin(SERVER_USAGE).on(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(SERVER_USAGE.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.SERVER_USAGE.name())))
                .leftJoin(SERVER_INFORMATION).on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                .leftJoin(SERVER_OWNING_APP).on(SERVER_USAGE.ENTITY_ID.eq(SERVER_OWNING_APP.ID)
                        .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())))

                .leftJoin(DATABASE_USAGE).on(CUSTOM_ENVIRONMENT_USAGE.ENTITY_ID.eq(DATABASE_USAGE.ID)
                        .and(CUSTOM_ENVIRONMENT_USAGE.ENTITY_KIND.eq(EntityKind.DATABASE_USAGE.name())))
                .leftJoin(DATABASE_INFORMATION).on(DATABASE_USAGE.DATABASE_ID.eq(DATABASE_INFORMATION.ID))
                .leftJoin(DATABASE_OWNING_APP).on(DATABASE_USAGE.ENTITY_ID.eq(DATABASE_OWNING_APP.ID)
                        .and(DATABASE_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())));
    }
}
