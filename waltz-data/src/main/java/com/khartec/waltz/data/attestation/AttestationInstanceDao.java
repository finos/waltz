/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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

package com.khartec.waltz.data.attestation;

import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.attestation.ImmutableAttestationInstance;
import com.khartec.waltz.schema.tables.records.AttestationInstanceRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.ATTESTATION_INSTANCE;
import static com.khartec.waltz.schema.Tables.ATTESTATION_INSTANCE_RECIPIENT;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;


@Repository
public class AttestationInstanceDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            ATTESTATION_INSTANCE.PARENT_ENTITY_ID,
            ATTESTATION_INSTANCE.PARENT_ENTITY_KIND,
            newArrayList(EntityKind.values()))
            .as("entity_name");

    private final DSLContext dsl;

    private static final RecordMapper<Record, AttestationInstance> TO_DOMAIN_MAPPER = r -> {
        AttestationInstanceRecord record = r.into(ATTESTATION_INSTANCE);
        return ImmutableAttestationInstance.builder()
                .id(record.getId())
                .attestationRunId(record.getAttestationRunId())
                .parentEntity(EntityReference.mkRef(
                        EntityKind.valueOf(record.getParentEntityKind()),
                        record.getParentEntityId(),
                        r.getValue(ENTITY_NAME_FIELD)))
                .attestedAt(Optional.ofNullable(record.getAttestedAt()).map(ts -> ts.toLocalDateTime()))
                .attestedBy(Optional.ofNullable(record.getAttestedBy()))
                .build();
    };


    @Autowired
    public AttestationInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public AttestationInstance getById (long id) {

        return dsl.select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.ID.eq(id))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public long create(AttestationInstance attestationInstance) {
        checkNotNull(attestationInstance, "attestationInstance cannot be null");

        AttestationInstanceRecord record = dsl.newRecord(ATTESTATION_INSTANCE);
        record.setAttestationRunId(attestationInstance.attestationRunId());
        record.setParentEntityKind(attestationInstance.parentEntity().kind().name());
        record.setParentEntityId(attestationInstance.parentEntity().id());

        record.store();

        return record.getId();
    }


    public List<AttestationInstance> findByRecipient(String userId, boolean unattestedOnly) {
        Condition condition = ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId);
        if(unattestedOnly) {
            condition = condition.and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull());
        }

        return dsl.select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(condition)
                .fetch(TO_DOMAIN_MAPPER);
    }


    /**
     * find historically completed attestations for all parent refs which are currently pending attestation
     * by the provided user
     * @param userId
     * @return
     */
    public List<AttestationInstance> findHistoricalForPendingByUserId(String userId) {
        // fetch the parent refs for attestations currently pending for the user
        Select<Record2<String, Long>> currentlyPendingAttestationParentRefs = dsl
                .selectDistinct(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND, ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull());

        // get the historically completed atttestations based on the above list of parent refs
        return dsl.select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(currentlyPendingAttestationParentRefs)
                .on(currentlyPendingAttestationParentRefs.field(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND).eq(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND)
                        .and(currentlyPendingAttestationParentRefs.field(ATTESTATION_INSTANCE.PARENT_ENTITY_ID).eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID))
                )
                .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNotNull())
                .orderBy(ATTESTATION_INSTANCE.ATTESTED_AT.desc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AttestationInstance> findByEntityReference(EntityReference ref) {
        return dsl.select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean attestInstance(long instanceId, String attestedBy, LocalDateTime dateTime) {
        return dsl.update(ATTESTATION_INSTANCE)
                .set(ATTESTATION_INSTANCE.ATTESTED_BY, attestedBy)
                .set(ATTESTATION_INSTANCE.ATTESTED_AT, Timestamp.valueOf(dateTime))
                .where(ATTESTATION_INSTANCE.ID.eq(instanceId).and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull()))
                .execute() == 1;
    }


    public List<AttestationInstance> findByRunId(long runId) {
        return dsl.select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(runId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public int cleanupOrphans() {

        Select<Record1<Long>> orphanAttestationIds = DSL.selectDistinct(ATTESTATION_INSTANCE.ID)
                .from(ATTESTATION_INSTANCE)
                .leftJoin(APPLICATION)
                .on(APPLICATION.ID.eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID)
                        .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .where(ATTESTATION_INSTANCE.ATTESTED_AT.isNull())
                .and(APPLICATION.ID.isNull()
                        .or(APPLICATION.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.REMOVED.name()))
                        .or(APPLICATION.IS_REMOVED.eq(true)));

        dsl.deleteFrom(ATTESTATION_INSTANCE_RECIPIENT)
                .where(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.in(orphanAttestationIds))
                .execute();

        int numberOfInstancesDeleted = dsl.deleteFrom(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.ID.in(orphanAttestationIds))
                .execute();

        return numberOfInstancesDeleted;
    }
}