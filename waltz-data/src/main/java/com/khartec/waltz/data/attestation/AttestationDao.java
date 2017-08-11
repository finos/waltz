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
import com.khartec.waltz.model.attestation.ImmutableAttestation;
import com.khartec.waltz.schema.tables.records.AttestationRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.Attestation.ATTESTATION;


@Repository
public class AttestationDao {

    private final DSLContext dsl;

    public static final RecordMapper<Record, Attestation> TO_ATTESTATION_MAPPER = r -> {
        AttestationRecord record = r.into(AttestationRecord.class);

        return ImmutableAttestation.builder()
                .id(record.getId())
                .parentEntityReference(mkRef(EntityKind.valueOf(record.getParentEntityKind()), record.getParentEntityId()))
                .childKind(EntityKind.valueOf(record.getChildEntityKind()))
                .attestedBy(record.getAttestedBy())
                .attestedAt(record.getAttestedAt().toLocalDateTime())
                .comments(record.getComments())
                .build();
    };


    public static final Function<Attestation, AttestationRecord> TO_RECORD_MAPPER = at -> {

        AttestationRecord record = new AttestationRecord();
        record.setParentEntityKind(at.parentEntityReference().kind().name());
        record.setParentEntityId(at.parentEntityReference().id());
        record.setChildEntityKind(at.childKind().name());
        record.setAttestedBy(at.attestedBy());
        record.setAttestedAt(Timestamp.valueOf(at.attestedAt()));
        record.setComments(at.comments().orElse(null));
        at.id().ifPresent(record::setId);

        return record;
    };


    @Autowired
    public AttestationDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<Attestation> findForEntity(EntityReference reference) {
        checkNotNull(reference, "reference cannot be null");

        return dsl
                .select(ATTESTATION.fields())
                .from(ATTESTATION)
                .where(ATTESTATION.PARENT_ENTITY_KIND.eq(reference.kind().name())
                        .and(ATTESTATION.PARENT_ENTITY_ID.eq(reference.id())))
                .fetch(TO_ATTESTATION_MAPPER);
    }


    public boolean create(Attestation attestation) {
        checkNotNull(attestation, "attestation cannot be null");

        AttestationRecord attestationRecord = TO_RECORD_MAPPER.apply(attestation);
        return dsl.executeInsert(attestationRecord) == 1;
    }
}
