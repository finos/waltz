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

package org.finos.waltz.data.taxonomy_management;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.data.JooqUtilities;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.taxonomy_management.ImmutableTaxonomyChangeCommand;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeCommand;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeLifecycleStatus;
import org.finos.waltz.model.taxonomy_management.TaxonomyChangeType;
import org.finos.waltz.schema.tables.records.TaxonomyChangeRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDateTime;
import static org.finos.waltz.common.JacksonUtilities.getJsonMapper;
import static org.finos.waltz.common.SetUtilities.asSet;
import static org.finos.waltz.schema.tables.TaxonomyChange.TAXONOMY_CHANGE;


@Repository
public class TaxonomyChangeDao {

    private static final Field<String> PRIMARY_REF_NAME = InlineSelectFieldFactory.mkNameField(
            TAXONOMY_CHANGE.PRIMARY_REFERENCE_ID,
            TAXONOMY_CHANGE.PRIMARY_REFERENCE_KIND,
            asSet(EntityKind.MEASURABLE)).as("primaryRefName");

    private static final Field<String> CHANGE_DOMAIN_NAME = InlineSelectFieldFactory.mkNameField(
            TAXONOMY_CHANGE.DOMAIN_ID,
            TAXONOMY_CHANGE.DOMAIN_KIND,
            asSet(EntityKind.MEASURABLE_CATEGORY)).as("changeDomainName");


    private static final RecordMapper<Record, TaxonomyChangeCommand> TO_DOMAIN_MAPPER = r ->
            ImmutableTaxonomyChangeCommand.builder()
                    .id(r.get(TAXONOMY_CHANGE.ID))
                    .changeType(TaxonomyChangeType.valueOf(r.get(TAXONOMY_CHANGE.CHANGE_TYPE)))
                    .status(TaxonomyChangeLifecycleStatus.valueOf(r.get(TAXONOMY_CHANGE.STATUS)))
                    .params(readParams(r))
                    .primaryReference(JooqUtilities.readRef(r, TAXONOMY_CHANGE.PRIMARY_REFERENCE_KIND, TAXONOMY_CHANGE.PRIMARY_REFERENCE_ID, PRIMARY_REF_NAME))
                    .changeDomain(JooqUtilities.readRef(r, TAXONOMY_CHANGE.DOMAIN_KIND, TAXONOMY_CHANGE.DOMAIN_ID, CHANGE_DOMAIN_NAME))
                    .createdBy(r.get(TAXONOMY_CHANGE.CREATED_BY))
                    .createdAt(toLocalDateTime(r.get(TAXONOMY_CHANGE.CREATED_AT)))
                    .lastUpdatedBy(r.get(TAXONOMY_CHANGE.LAST_UPDATED_BY))
                    .lastUpdatedAt(toLocalDateTime(r.get(TAXONOMY_CHANGE.LAST_UPDATED_AT)))
                    .build();



    private static final BiFunction<TaxonomyChangeCommand, DSLContext, TaxonomyChangeRecord> TO_RECORD_MAPPER = (cmd, dsl) -> {
        TaxonomyChangeRecord r = dsl.newRecord(TAXONOMY_CHANGE);
        cmd.id().ifPresent(id -> {
            r.setId(id);
            r.changed(TAXONOMY_CHANGE.ID, false);
        });
        r.setStatus(cmd.status().name());
        r.setChangeType(cmd.changeType().name());
        r.setPrimaryReferenceKind(cmd.primaryReference().kind().name());
        r.setPrimaryReferenceId(cmd.primaryReference().id());
        r.setDomainKind(cmd.changeDomain().kind().name());
        r.setDomainId(cmd.changeDomain().id());
        r.setLastUpdatedAt(Timestamp.valueOf(cmd.lastUpdatedAt()));
        r.setLastUpdatedBy(cmd.lastUpdatedBy());
        r.setDescription(null);
        r.setParams(writeParams(cmd.params()));
        r.setCreatedAt(Timestamp.valueOf(cmd.createdAt()));
        r.setCreatedBy(cmd.createdBy());
        return r;
    };


    private static Map<String, ? extends String> readParams(Record r) {
        try {
            return getJsonMapper().readValue(
                    r.get(TAXONOMY_CHANGE.PARAMS),
                    Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }


    private static String writeParams(Map<String, String> map) {
        try {
            return getJsonMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }


    private final DSLContext dsl;


    @Autowired
    public TaxonomyChangeDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public boolean removeById(long id, String userId) {
        return dsl
                .update(TAXONOMY_CHANGE)
                .set(TAXONOMY_CHANGE.STATUS, TaxonomyChangeLifecycleStatus.ABORTED.name())
                .set(TAXONOMY_CHANGE.LAST_UPDATED_AT, nowUtcTimestamp())
                .set(TAXONOMY_CHANGE.LAST_UPDATED_BY, userId)
                .where(TAXONOMY_CHANGE.ID.eq(id))
                .execute() == 1;
    }


    public TaxonomyChangeCommand getDraftCommandById(long id) {
        return dsl
                .select(TAXONOMY_CHANGE.fields())
                .select(PRIMARY_REF_NAME, CHANGE_DOMAIN_NAME)
                .from(TAXONOMY_CHANGE)
                .where(TAXONOMY_CHANGE.ID.eq(id))
                .and(TAXONOMY_CHANGE.STATUS.eq(TaxonomyChangeLifecycleStatus.DRAFT.name()))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public TaxonomyChangeCommand createCommand(TaxonomyChangeCommand cmd) {
        TaxonomyChangeRecord r = TO_RECORD_MAPPER.apply(cmd, dsl);
        r.insert();
        return ImmutableTaxonomyChangeCommand
                .copyOf(cmd)
                .withId(r.getId());
    }

    public Collection<TaxonomyChangeCommand> findChangesByDomainAndStatus(EntityReference domain, TaxonomyChangeLifecycleStatus status) {
        return dsl
                .select(TAXONOMY_CHANGE.fields())
                .select(PRIMARY_REF_NAME, CHANGE_DOMAIN_NAME)
                .from(TAXONOMY_CHANGE)
                .where(TAXONOMY_CHANGE.STATUS.eq(status.name()))
                .and(TAXONOMY_CHANGE.DOMAIN_ID.eq(domain.id()))
                .and(TAXONOMY_CHANGE.DOMAIN_KIND.eq(domain.kind().name()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public TaxonomyChangeCommand update(TaxonomyChangeCommand cmd) {
        TaxonomyChangeRecord r = TO_RECORD_MAPPER.apply(cmd, dsl);
        r.update();
        return cmd;
    }
}
