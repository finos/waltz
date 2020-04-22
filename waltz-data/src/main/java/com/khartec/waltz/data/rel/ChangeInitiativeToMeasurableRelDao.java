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

package com.khartec.waltz.data.rel;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.rel.ChangeInitiativeToMeasurableRel;
import com.khartec.waltz.model.rel.CreateRelationshipCommand;
import com.khartec.waltz.model.rel.ImmutableChangeInitiativeToMeasurableRel;
import com.khartec.waltz.schema.tables.records.ChangeInitiativeToMeasurableRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

import static com.khartec.waltz.schema.Tables.CHANGE_INITIATIVE_TO_MEASURABLE;

@Repository
public class ChangeInitiativeToMeasurableRelDao {

    private static final RecordMapper<Record, ChangeInitiativeToMeasurableRel> TO_DOMAIN_MAPPER = r -> {
        ChangeInitiativeToMeasurableRecord record = r.into(ChangeInitiativeToMeasurableRecord.class);
        return ImmutableChangeInitiativeToMeasurableRel
                .builder()
                .id(record.getId())
                .changeInitiativeId(record.getChangeInitiativeId())
                .measurableId(record.getMeasurableId())
                .relationshipId(record.getRelationshipId())
                .description(record.getDescription())
                .provenance(record.getProvenance())
                .created(UserTimestamp.mkForUser(
                        record.getLastUpdatedBy(),
                        record.getLastUpdatedAt()))
                .lastUpdated(UserTimestamp.mkForUser(
                        record.getLastUpdatedBy(),
                        record.getLastUpdatedAt()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public ChangeInitiativeToMeasurableRelDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<ChangeInitiativeToMeasurableRel> findForChangeInitiativeSelector(Select<Record1<Long>> selector) {
        return findByCondition(CHANGE_INITIATIVE_TO_MEASURABLE.CHANGE_INITIATIVE_ID.in(selector));
    }


    public Set<ChangeInitiativeToMeasurableRel> findForMeasurableSelector(Select<Record1<Long>> selector) {
        return findByCondition(CHANGE_INITIATIVE_TO_MEASURABLE.MEASURABLE_ID.in(selector));
    }


    private Set<ChangeInitiativeToMeasurableRel> findByCondition(Condition cond) {
        return dsl
                .select(CHANGE_INITIATIVE_TO_MEASURABLE.fields())
                .from(CHANGE_INITIATIVE_TO_MEASURABLE)
                .where(cond)
                .fetchSet(TO_DOMAIN_MAPPER);
    }


    public Long createRelationship(CreateRelationshipCommand cmd, String userId) {
        ChangeInitiativeToMeasurableRecord record = dsl.newRecord(CHANGE_INITIATIVE_TO_MEASURABLE);

        record.setChangeInitiativeId(cmd.idA());
        record.setMeasurableId(cmd.idB());
        record.setRelationshipId(cmd.relationshipKindId());

        record.setProvenance(cmd.provenance());
        record.setDescription(cmd.description());
        record.setCreatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setCreatedBy(userId);
        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
        record.setLastUpdatedBy(userId);

        record.store();
        return record.getId();
    }
}
