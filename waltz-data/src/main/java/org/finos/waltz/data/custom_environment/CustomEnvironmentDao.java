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

import com.khartec.waltz.schema.tables.records.CustomEnvironmentRecord;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.custom_environment.CustomEnvironment;
import org.finos.waltz.model.custom_environment.ImmutableCustomEnvironment;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.khartec.waltz.schema.Tables.CUSTOM_ENVIRONMENT;
import static org.finos.waltz.model.EntityKind.valueOf;
import static org.finos.waltz.model.EntityReference.mkRef;


@Repository
public class CustomEnvironmentDao {

    private final DSLContext dsl;

    private static final RecordMapper<Record, CustomEnvironment> TO_CUSTOM_ENV_MAPPER = r -> {
        CustomEnvironmentRecord record = r.into(CUSTOM_ENVIRONMENT);

        EntityReference ref = mkRef(valueOf(record.getOwningEntityKind()), record.getOwningEntityId());

        return ImmutableCustomEnvironment.builder()
                .id(record.getId())
                .owningEntity(ref)
                .name(record.getName())
                .description(Optional.ofNullable(record.getDescription()))
                .externalId(record.getExternalId())
                .groupName(record.getGroupName())
                .build();
    };


    private static final Function<CustomEnvironment, CustomEnvironmentRecord> TO_RECORD = environment -> {
        CustomEnvironmentRecord r = new CustomEnvironmentRecord();
        r.setName(environment.name());
        r.setDescription(environment.description().orElse(null));
        r.setExternalId(environment.externalId().get());
        r.setOwningEntityId(environment.owningEntity().id());
        r.setOwningEntityKind(environment.owningEntity().kind().name());
        r.setGroupName(environment.groupName());
        return r;
    };



    @Autowired
    public CustomEnvironmentDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<CustomEnvironment> findAll(){
        return dsl
                .select(CUSTOM_ENVIRONMENT.fields())
                .from(CUSTOM_ENVIRONMENT)
                .fetchSet(TO_CUSTOM_ENV_MAPPER);
    }


    public Set<CustomEnvironment> findByOwningEntityRef(EntityReference ref){
        return dsl
                .select(CUSTOM_ENVIRONMENT.fields())
                .from(CUSTOM_ENVIRONMENT)
                .where(CUSTOM_ENVIRONMENT.OWNING_ENTITY_ID.eq(ref.id())
                        .and(CUSTOM_ENVIRONMENT.OWNING_ENTITY_KIND.eq(ref.kind().name())))
                .fetchSet(TO_CUSTOM_ENV_MAPPER);
    }


    public Long create(CustomEnvironment environment) {

        CustomEnvironmentRecord record = new CustomEnvironmentRecord();
        record.setName(environment.name());
        record.setDescription(environment.description().orElse(null));
        record.setExternalId(environment.externalId().get());
        record.setOwningEntityId(environment.owningEntity().id());
        record.setOwningEntityKind(environment.owningEntity().kind().name());
        record.setGroupName(environment.groupName());

        return  dsl
                .insertInto(CUSTOM_ENVIRONMENT)
                .set(record)
                .returning(CUSTOM_ENVIRONMENT.ID)
                .fetchOne()
                .getId();
    }


    public boolean remove(Long envId){
        return dsl
                .delete(CUSTOM_ENVIRONMENT)
                .where(CUSTOM_ENVIRONMENT.ID.eq(envId))
                .execute() == 1;
    }


    public CustomEnvironment getById(Long id) {
        return dsl
                .select(CUSTOM_ENVIRONMENT.fields())
                .from(CUSTOM_ENVIRONMENT)
                .where(CUSTOM_ENVIRONMENT.ID.eq(id))
                .fetchOne(TO_CUSTOM_ENV_MAPPER);
    }
}
