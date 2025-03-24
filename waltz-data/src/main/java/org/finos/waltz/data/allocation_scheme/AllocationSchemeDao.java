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

package org.finos.waltz.data.allocation_scheme;

import org.finos.waltz.schema.tables.records.AllocationSchemeRecord;
import org.finos.waltz.model.allocation_scheme.AllocationScheme;
import org.finos.waltz.model.allocation_scheme.ImmutableAllocationScheme;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.finos.waltz.schema.tables.AllocationScheme.ALLOCATION_SCHEME;
import static java.util.Optional.ofNullable;

@Repository
public class AllocationSchemeDao {

    private final DSLContext dsl;


    public static final RecordMapper<Record, AllocationScheme> TO_DOMAIN_MAPPER = record -> {
        AllocationSchemeRecord allocationSchemeRecord = record.into(ALLOCATION_SCHEME);
        return ImmutableAllocationScheme.builder()
                .id(allocationSchemeRecord.getId())
                .name(allocationSchemeRecord.getName())
                .description(allocationSchemeRecord.getDescription())
                .measurableCategoryId(allocationSchemeRecord.getMeasurableCategoryId())
                .externalId(ofNullable(allocationSchemeRecord.getExternalId()))
                .build();
    };


    @Autowired
    public AllocationSchemeDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public AllocationScheme getById(long id){
        return dsl
                .select(ALLOCATION_SCHEME.fields())
                .from(ALLOCATION_SCHEME)
                .where(ALLOCATION_SCHEME.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }

    public AllocationScheme getByExternalId(String externalId) {
        return dsl
                .select(ALLOCATION_SCHEME.fields())
                .from(ALLOCATION_SCHEME)
                .where(ALLOCATION_SCHEME.EXTERNAL_ID.eq(externalId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<AllocationScheme> findAll(){
        return dsl
                .select(ALLOCATION_SCHEME.fields())
                .from(ALLOCATION_SCHEME)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AllocationScheme> findByCategoryId(long categoryId){
        return dsl
                .select(ALLOCATION_SCHEME.fields())
                .from(ALLOCATION_SCHEME)
                .where(ALLOCATION_SCHEME.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public Long create(AllocationScheme scheme) {
        AllocationSchemeRecord record = dsl.newRecord(ALLOCATION_SCHEME);
        record.setDescription(scheme.description());
        record.setMeasurableCategoryId(scheme.measurableCategoryId());
        record.setName(scheme.name());
        record.setExternalId(scheme.externalId().orElse(""));
        record.insert();
        return record.getId();
    }
}
