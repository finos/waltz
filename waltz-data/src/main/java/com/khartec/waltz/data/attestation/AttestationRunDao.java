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
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.HierarchyQueryScope;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.attestation.*;
import com.khartec.waltz.schema.tables.records.AttestationRunRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.toSqlDate;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.StringUtilities.join;
import static com.khartec.waltz.common.StringUtilities.splitThenMap;
import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static com.khartec.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static com.khartec.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;

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

    private static final RecordMapper<Record, AttestationRun> TO_DOMAIN_MAPPER = r -> {
        AttestationRunRecord record = r.into(ATTESTATION_RUN);

        Optional<EntityReference> attestedEntityRef = Optional.empty();
        if(record.getAttestedEntityKind() != null && record.getAttestedEntityId() != null) {
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
                .involvementKindIds(splitThenMap(
                        record.getInvolvementKindIds(),
                        ID_SEPARATOR,
                        Long::valueOf))
                .issuedBy(record.getIssuedBy())
                .issuedOn(record.getIssuedOn().toLocalDate())
                .dueDate(record.getDueDate().toLocalDate())
                .attestedEntityKind(EntityKind.valueOf(record.getAttestedEntityKind()))
                .attestedEntityRef(attestedEntityRef)
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
        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .where(ATTESTATION_RUN.ID.eq(attestationRunId))
                .fetchOne(TO_DOMAIN_MAPPER);
    }


    public List<AttestationRun> findAll() {
        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AttestationRun> findByRecipient(String userId) {
        return dsl.selectDistinct(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE)
                    .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                    .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<AttestationRun> findByEntityReference(EntityReference ref) {
        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE)
                    .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(ref.id()))
                .fetch(TO_DOMAIN_MAPPER);
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

        return dsl.select(ATTESTATION_RUN.fields())
                .select(ENTITY_NAME_FIELD)
                .select(ATTESTED_ENTITY_NAME_FIELD)
                .from(ATTESTATION_RUN)
                .innerJoin(ATTESTATION_INSTANCE)
                .on(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                .where(ATTESTATION_INSTANCE.ID.in(selector))
                .fetch(TO_DOMAIN_MAPPER);
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
        record.setInvolvementKindIds(join(command.involvementKindIds(), ID_SEPARATOR));
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
}
