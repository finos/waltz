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

package org.finos.waltz.data.attestation;

import org.finos.waltz.data.involvement_group.InvolvementGroupDao;
import org.finos.waltz.schema.tables.records.AttestationRunRecord;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.attestation.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.finos.waltz.schema.Tables.SURVEY_RUN;
import static org.finos.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static org.finos.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static org.finos.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.DateTimeUtilities.*;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.StringUtilities.join;

@Repository
public class AttestationRunDao {

    private static final Field<String> ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            ATTESTATION_RUN.SELECTOR_ENTITY_ID,
            ATTESTATION_RUN.SELECTOR_ENTITY_KIND,
            newArrayList(EntityKind.values()))
            .as("entity_name");

    private static final Field<String> ATTESTED_ENTITY_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
            ATTESTATION_RUN.ATTESTED_ENTITY_ID,
            ATTESTATION_RUN.ATTESTED_ENTITY_KIND,
            newArrayList(EntityKind.values()))
            .as("attested_entity_name");

    private static final Field<BigDecimal> COMPLETE_SUM = DSL.sum(DSL
            .when(ATTESTATION_INSTANCE.ATTESTED_BY.isNotNull(), DSL.val(1))
            .otherwise(DSL.val(0))).as("Complete");

    private static final Field<BigDecimal> PENDING_SUM = DSL.sum(DSL
            .when(ATTESTATION_INSTANCE.ATTESTED_BY.isNull(), DSL.val(1))
            .otherwise(DSL.val(0))).as("Pending");

    private static final String ID_SEPARATOR = ";";

    private static AttestationRun mkAttestationRun(Record r, Map<Long, List<Long>> attestationInvolvementGroupKindIds) {

        AttestationRunRecord record = r.into(ATTESTATION_RUN);

        Long recipientInvolvementGroupId = record.getRecipientInvolvementGroupId();
        List<Long> recipients = attestationInvolvementGroupKindIds.getOrDefault(recipientInvolvementGroupId, emptyList());


        Optional<EntityReference> attestedEntityRef = Optional.empty();
        if (record.getAttestedEntityKind() != null && record.getAttestedEntityId() != null) {
            attestedEntityRef = Optional.of(EntityReference.mkRef(
                    EntityKind.valueOf(record.getAttestedEntityKind()),
                    record.getAttestedEntityId(),
                    r.getValue(ATTESTED_ENTITY_NAME_FIELD)));
        }

        return ImmutableAttestationRun.builder()
                .id(record.getId())
                .targetEntityKind(EntityKind.valueOf(record.getTargetEntityKind()))
                .name(record.getName())
                .description(record.getDescription())
                .selectionOptions(IdSelectionOptions.mkOpts(
                        EntityReference.mkRef(
                                EntityKind.valueOf(record.getSelectorEntityKind()),
                                record.getSelectorEntityId(),
                                r.getValue(ENTITY_NAME_FIELD)),
                        HierarchyQueryScope.valueOf(record.getSelectorHierarchyScope())))
                .involvementKindIds(recipients)
                .issuedBy(record.getIssuedBy())
                .issuedOn(toLocalDate(record.getIssuedOn()))
                .dueDate(toLocalDate(record.getDueDate()))
                .attestedEntityKind(EntityKind.valueOf(record.getAttestedEntityKind()))
                .attestedEntityRef(attestedEntityRef)
                .status(AttestationStatus.valueOf(record.getStatus()))
                .provenance(record.getProvenance())
                .build();
    };


    private static final RecordMapper<Record, AttestationRunResponseSummary> TO_RESPONSE_SUMMARY_MAPPER = r -> {
        Field<Long> runId = ATTESTATION_RUN.ID.field(r);

        return ImmutableAttestationRunResponseSummary.builder()
                .runId(r.get(runId))
                .completeCount(r.get(COMPLETE_SUM).longValue())
                .pendingCount(r.get(PENDING_SUM).longValue())
                .build();
    };

    private final DSLContext dsl;


    @Autowired
    public AttestationRunDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public AttestationRun getById(long attestationRunId) {

        Map<Long, List<Long>> involvementsByGroupId = InvolvementGroupDao.findAllInvolvementsByGroupId(dsl);

        return dsl
                .select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .where(ATTESTATION_RUN.ID.eq(attestationRunId))
                .fetchOne(r -> mkAttestationRun(r, involvementsByGroupId));
    }


    public List<AttestationRun> findAll() {

        Map<Long, List<Long>> involvementsByGroupId = InvolvementGroupDao.findAllInvolvementsByGroupId(dsl);

        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .fetch(r -> mkAttestationRun(r, involvementsByGroupId));
    }


    public List<AttestationRun> findByRecipient(String userId) {

        Map<Long, List<Long>> involvementsByGroupId = InvolvementGroupDao.findAllInvolvementsByGroupId(dsl);

        return dsl.selectDistinct(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .fetch(r -> mkAttestationRun(r, involvementsByGroupId));
    }


    public List<AttestationRun> findByEntityReference(EntityReference ref) {

        Map<Long, List<Long>> involvementsByGroupId = InvolvementGroupDao.findAllInvolvementsByGroupId(dsl);

        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(ref.id()))
                .fetch(r -> mkAttestationRun(r, involvementsByGroupId));
    }


    public List<AttestationRunResponseSummary> findResponseSummaries() {
        return dsl.select(
                    ATTESTATION_RUN.ID,
                    COMPLETE_SUM,
                    PENDING_SUM)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE).on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .groupBy(ATTESTATION_RUN.ID)
                .fetch(TO_RESPONSE_SUMMARY_MAPPER);
    }


    public List<AttestationRun> findByIdSelector(Select<Record1<Long>> selector) {

        Map<Long, List<Long>> involvementsByGroupId = InvolvementGroupDao.findAllInvolvementsByGroupId(dsl);

        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .where(ATTESTATION_INSTANCE.ID.in(selector))
                .fetch(r -> mkAttestationRun(r, involvementsByGroupId));
    }

    public Long create(String userId, AttestationRunCreateCommand command) {
        checkNotNull(command, "command cannot be null");

        AttestationRunRecord record = dsl.newRecord(ATTESTATION_RUN);
        record.setTargetEntityKind(command.targetEntityKind().name());
        record.setName(command.name());
        record.setDescription(command.description());
        record.setSelectorEntityKind(command.selectionOptions().entityReference().kind().name());
        record.setSelectorEntityId(command.selectionOptions().entityReference().id());
        record.setSelectorHierarchyScope(command.selectionOptions().scope().name());
        record.setIssuedBy(userId);
        record.setIssuedOn(toSqlDate(command.issuedOn()));
        record.setDueDate(toSqlDate(command.dueDate()));
        record.setAttestedEntityKind(command.attestedEntityKind().name());
        record.setAttestedEntityId(command.attestedEntityId().orElse(null));

        record.store();

        return record.getId();
    }


    public int getEntityCount(Select<Record1<Long>> idSelector) {
        Field<Integer> entityCount = DSL.count().as("entity_count");
        return dsl.select(entityCount)
                .from(idSelector)
                .fetchOne(r -> r.get(entityCount));
    }


    public Set<AttestationRun> findPendingRuns() {

        Map<Long, List<Long>> involvementsByGroupId = InvolvementGroupDao.findAllInvolvementsByGroupId(dsl);

        return dsl
                .select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .where(ATTESTATION_RUN.STATUS.eq(AttestationStatus.PENDING.name()))
                .fetchSet(r -> mkAttestationRun(r, involvementsByGroupId));
    }


    public int updateStatusForRunIds(Set<Long> runIds, AttestationStatus newStatus) {

        if (AttestationStatus.ISSUED.equals(newStatus)){
            return dsl
                    .update(ATTESTATION_RUN)
                    .set(ATTESTATION_RUN.STATUS, newStatus.name())
                    .set(ATTESTATION_RUN.ISSUED_BY, "admin")
                    .set(ATTESTATION_RUN.ISSUED_ON, toSqlDate(nowUtcTimestamp()))
                    .where(ATTESTATION_RUN.ID.in(runIds))
                    .execute();
        } else {
            return dsl
                    .update(ATTESTATION_RUN)
                    .set(ATTESTATION_RUN.STATUS, newStatus.name())
                    .where(ATTESTATION_RUN.ID.in(runIds))
                    .execute();
        }
    }

    public Long getRecipientInvolvementGroupId(long attestationRunId) {
        return dsl
                .select(ATTESTATION_RUN.RECIPIENT_INVOLVEMENT_GROUP_ID)
                .from(ATTESTATION_RUN)
                .where(ATTESTATION_RUN.ID.eq(attestationRunId))
                .fetchOne(ATTESTATION_RUN.RECIPIENT_INVOLVEMENT_GROUP_ID);
    }

    public int updateRecipientInvolvementGroupId(long attestationRunId, Long recipientInvGroupId) {
        return dsl
                .update(ATTESTATION_RUN)
                .set(ATTESTATION_RUN.RECIPIENT_INVOLVEMENT_GROUP_ID, recipientInvGroupId)
                .where(ATTESTATION_RUN.ID.eq(attestationRunId))
                .execute();
    }
}
