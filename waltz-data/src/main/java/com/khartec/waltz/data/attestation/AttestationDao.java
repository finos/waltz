/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.Attestation;
import com.khartec.waltz.model.attestation.AttestationType;
import com.khartec.waltz.model.attestation.ImmutableAttestation;
import com.khartec.waltz.schema.tables.records.AttestationRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.valueOf;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Attestation.ATTESTATION;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;


@Repository
public class AttestationDao {

    private final DSLContext dsl;


    public static final RecordMapper<AttestationRecord, Attestation> TO_ATTESTATION_MAPPER = record -> {
        EntityKind entityKind = Enum.valueOf(EntityKind.class, record.getEntityKind());

        EntityReference attestingEntityRef = null;
        Long attestingEntityId = record.getAttestingEntityId();
        String attestingEntityKindStr = record.getAttestingEntityKind();

        if (attestingEntityId != null
                && attestingEntityKindStr != null) {
            attestingEntityRef = mkRef(
                    valueOf(attestingEntityKindStr),
                    attestingEntityId);
        }

        return ImmutableAttestation.builder()
                .id(record.getId())
                .entityReference(mkRef(entityKind, record.getEntityId()))
                .attestationType(Enum.valueOf(AttestationType.class, record.getAttestationType()))
                .attestedBy(record.getAttestedBy())
                .attestedAt(record.getAttestedAt().toLocalDateTime())
                .attestingEntityReference(Optional.ofNullable(attestingEntityRef))
                .comments(record.getComments())
                .provenance(record.getProvenance())
                .build();
    };


    public static final Function<Attestation, AttestationRecord> TO_RECORD_MAPPER = at -> {

        AttestationRecord record = new AttestationRecord();
        record.setEntityKind(at.entityReference().kind().name());
        record.setEntityId(at.entityReference().id());
        record.setAttestationType(at.attestationType().name());
        record.setAttestedBy(at.attestedBy());
        record.setAttestedAt(Timestamp.valueOf(at.attestedAt()));
        record.setComments(at.comments());
        record.setProvenance(at.provenance());
        at.id().ifPresent(record::setId);
        at.attestingEntityReference().ifPresent(e -> record.setAttestingEntityId(e.id()));
        at.attestingEntityReference().ifPresent(e -> record.setAttestingEntityKind(e.kind().name()));

        return record;
    };


    @Autowired
    public AttestationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<Attestation> findForEntity(EntityReference reference) {
        checkNotNull(reference, "reference cannot be null");

        return dsl.selectFrom(ATTESTATION)
                .where(ATTESTATION.ENTITY_KIND.eq(reference.kind().name())
                        .and(ATTESTATION.ENTITY_ID.eq(reference.id())))
                .fetch(TO_ATTESTATION_MAPPER);
    }


    public boolean create(Attestation attestation) {
        checkNotNull(attestation, "attestation cannot be null");

        AttestationRecord attestationRecord = TO_RECORD_MAPPER.apply(attestation);
        return dsl.executeInsert(attestationRecord) == 1;
    }


    public boolean deleteForEntity(EntityReference reference) {
        checkNotNull(reference, "reference cannot be null");

        return dsl.deleteFrom(ATTESTATION)
                .where(ATTESTATION.ENTITY_KIND.eq(reference.kind().name())
                        .and(ATTESTATION.ENTITY_ID.eq(reference.id())))
                .execute() > 0;
    }


    public boolean recalculateForFlowDiagrams() {
        final String provenance = "flow diagram";

        dsl.transaction(configuration -> {
            DSLContext tx = DSL.using(configuration);

            Condition isImpliedFlowDiagramAttestation = ATTESTATION.ATTESTATION_TYPE.eq(AttestationType.IMPLICIT.name())
                    .and(ATTESTATION.PROVENANCE.eq(provenance));

            // clear existing attestations
            tx.deleteFrom(ATTESTATION)
                    .where(isImpliedFlowDiagramAttestation)
                    .execute();

            Select<Record9<String, Long, String, String, Timestamp, String, String, Long, String>> entities = DSL.select(
                    FLOW_DIAGRAM_ENTITY.ENTITY_KIND,
                    FLOW_DIAGRAM_ENTITY.ENTITY_ID,
                    DSL.val(AttestationType.IMPLICIT.name()).as("attestation_type"),
                    FLOW_DIAGRAM.LAST_UPDATED_BY.as("attested_by"),
                    FLOW_DIAGRAM.LAST_UPDATED_AT.as("attested_at"),
                    DSL.val("Attested by flow diagram inclusion").as("comments"),
                    DSL.val(provenance).as("provenance"),
                    FLOW_DIAGRAM.ID.as("attesting_entity_id"),
                    DSL.val(EntityKind.FLOW_DIAGRAM.name()).as("attesting_entity_kind"))
                    .from(FLOW_DIAGRAM_ENTITY)
                    .innerJoin(FLOW_DIAGRAM)
                    .on(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(FLOW_DIAGRAM.ID))
                    .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.in(
                            EntityKind.LOGICAL_DATA_FLOW.name(),
                            EntityKind.PHYSICAL_FLOW.name()));

            insertAttestations(tx, entities);
        });

        return true;
    }


    public boolean recalculateForLogicalFlowDecorators() {
        final String provenance = "logical flow decoration";

        dsl.transaction(configuration -> {
            DSLContext tx = DSL.using(configuration);

            Condition isImpliedDecoratorAttestation = ATTESTATION.ENTITY_KIND.eq(EntityKind.LOGICAL_DATA_FLOW.name())
                    .and(ATTESTATION.ATTESTATION_TYPE.eq(AttestationType.IMPLICIT.name()))
                    .and(ATTESTATION.ATTESTING_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()))
                    .and(ATTESTATION.PROVENANCE.eq(provenance));

            // clear existing attestations
            tx.deleteFrom(ATTESTATION)
                    .where(isImpliedDecoratorAttestation)
                    .execute();

            Select<Record9<String, Long, String, String, Timestamp, String, String, Long, String>> select = DSL.select(
                    DSL.val(EntityKind.LOGICAL_DATA_FLOW.name()).as("entity_kind"),
                    LOGICAL_FLOW.ID.as("entity_id"),
                    DSL.val("IMPLICIT").as("attestation_type"),
                    LOGICAL_FLOW_DECORATOR.LAST_UPDATED_BY,
                    LOGICAL_FLOW_DECORATOR.LAST_UPDATED_AT,
                    DSL.val("Attestation by logical flow decoration").as("comments"),
                    DSL.val(provenance).as("provenance"),
                    LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.as("attesting_entity_id"),
                    LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.as("attesting_entity_kind"))
                    .from(LOGICAL_FLOW_DECORATOR)
                    .innerJoin(LOGICAL_FLOW)
                    .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID));

            insertAttestations(tx, select);
        });

        return true;
    }


    private void insertAttestations(DSLContext tx,
                                    Select<Record9<String, Long, String, String, Timestamp, String, String, Long, String>> attesationsSelector) {

        //insert into attestation(entity_kind, entity_id, attestation_type, attested_by, attested_at, comments, provenance, attesting_entity_id, attesting_entity_kind)
        tx.insertInto(ATTESTATION)
                .columns(ATTESTATION.ENTITY_KIND,
                        ATTESTATION.ENTITY_ID,
                        ATTESTATION.ATTESTATION_TYPE,
                        ATTESTATION.ATTESTED_BY,
                        ATTESTATION.ATTESTED_AT,
                        ATTESTATION.COMMENTS,
                        ATTESTATION.PROVENANCE,
                        ATTESTATION.ATTESTING_ENTITY_ID,
                        ATTESTATION.ATTESTING_ENTITY_KIND)
                .select(attesationsSelector)
                .execute();
    }

}
