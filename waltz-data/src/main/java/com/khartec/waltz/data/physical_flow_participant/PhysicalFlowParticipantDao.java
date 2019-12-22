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

package com.khartec.waltz.data.physical_flow_participant;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.physical_flow_participant.ImmutablePhysicalFlowParticipant;
import com.khartec.waltz.model.physical_flow_participant.ParticipationKind;
import com.khartec.waltz.model.physical_flow_participant.PhysicalFlowParticipant;
import com.khartec.waltz.schema.tables.records.PhysicalFlowParticipantRecord;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.PhysicalFlowParticipant.PHYSICAL_FLOW_PARTICIPANT;

@Repository
public class PhysicalFlowParticipantDao {

    private static final Logger LOG = LoggerFactory.getLogger(PhysicalFlowDao.class);

    private static final Field<String> PARTICIPANT_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID,
            PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND);

    public static final RecordMapper<Record, PhysicalFlowParticipant> TO_DOMAIN_MAPPER = r -> {
        PhysicalFlowParticipantRecord record = r.into(PHYSICAL_FLOW_PARTICIPANT);

        EntityReference ref = EntityReference.mkRef(
                EntityKind.valueOf(record.getParticipantEntityKind()),
                record.getParticipantEntityId(),
                r.get(PARTICIPANT_NAME_FIELD));

        return ImmutablePhysicalFlowParticipant.builder()
                .physicalFlowId(record.getPhysicalFlowId())
                .kind(ParticipationKind.valueOf(record.getKind()))
                .participant(ref)
                .provenance(record.getProvenance())
                .description(record.getDescription())
                .lastUpdatedBy(record.getLastUpdatedBy())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public PhysicalFlowParticipantDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public Collection<PhysicalFlowParticipant> findByPhysicalFlowId(long id) {
        return findByCondition(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID.eq(id));
    }


    public Collection<PhysicalFlowParticipant> findByParticipant(EntityReference entityReference) {
        return findByCondition(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID.eq(entityReference.id())
                .and(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND.eq(entityReference.kind().name())));
    }


    public boolean remove(long physicalFlowId, ParticipationKind participationKind, EntityReference participant) {
        return dsl
                .deleteFrom(PHYSICAL_FLOW_PARTICIPANT)
                .where(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID.eq(physicalFlowId))
                .and(PHYSICAL_FLOW_PARTICIPANT.KIND.eq(participationKind.name()))
                .and(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID.eq(participant.id()))
                .and(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND.eq(participant.kind().name()))
                .execute() > 0;
    }


    public boolean add(long physicalFlowId, ParticipationKind participationKind, EntityReference participant, String username) {
        return dsl
                .insertInto(PHYSICAL_FLOW_PARTICIPANT)
                .set(PHYSICAL_FLOW_PARTICIPANT.PHYSICAL_FLOW_ID, physicalFlowId)
                .set(PHYSICAL_FLOW_PARTICIPANT.KIND, participationKind.name())
                .set(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_KIND, participant.kind().name())
                .set(PHYSICAL_FLOW_PARTICIPANT.PARTICIPANT_ENTITY_ID, participant.id())
                .set(PHYSICAL_FLOW_PARTICIPANT.PROVENANCE, "waltz")
                .set(PHYSICAL_FLOW_PARTICIPANT.DESCRIPTION, "")
                .set(PHYSICAL_FLOW_PARTICIPANT.LAST_UPDATED_AT, DateTimeUtilities.nowUtcTimestamp())
                .set(PHYSICAL_FLOW_PARTICIPANT.LAST_UPDATED_BY, username)
                .execute() > 0;
    }


    private Collection<PhysicalFlowParticipant> findByCondition(Condition condition) {
        return dsl
                .select(PHYSICAL_FLOW_PARTICIPANT.fields())
                .select(PARTICIPANT_NAME_FIELD)
                .from(PHYSICAL_FLOW_PARTICIPANT)
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }
}
