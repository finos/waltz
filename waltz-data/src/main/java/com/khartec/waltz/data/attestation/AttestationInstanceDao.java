package com.khartec.waltz.data.attestation;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.attestation.AttestationInstance;
import com.khartec.waltz.model.attestation.ImmutableAttestationInstance;
import com.khartec.waltz.schema.tables.records.AttestationInstanceRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.data.EntityNameUtilities.mkEntityNameField;
import static com.khartec.waltz.schema.Tables.ATTESTATION_INSTANCE;
import static com.khartec.waltz.schema.Tables.ATTESTATION_INSTANCE_RECIPIENT;


@Repository
public class AttestationInstanceDao {

    private static final Field<String> ENTITY_NAME_FIELD = mkEntityNameField(
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
                .childEntityKind(EntityKind.valueOf(record.getChildEntityKind()))
                .attestedAt(Optional.ofNullable(record.getAttestedAt()).map(ts -> ts.toLocalDateTime()))
                .attestedBy(Optional.ofNullable(record.getAttestedBy()))
                .comments(record.getComments())
                .build();
    };


    @Autowired
    public AttestationInstanceDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public List<AttestationInstance> findByRecipient(String userId) {
        return dsl.select(ATTESTATION_INSTANCE.fields())
                .select(ENTITY_NAME_FIELD)
                .from(ATTESTATION_INSTANCE)
                .innerJoin(ATTESTATION_INSTANCE_RECIPIENT)
                .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .where(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.eq(userId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public boolean attestInstance(long instanceId, String attestedBy, LocalDateTime dateTime, String comments) {
        return dsl.update(ATTESTATION_INSTANCE)
            .set(ATTESTATION_INSTANCE.ATTESTED_BY, attestedBy)
            .set(ATTESTATION_INSTANCE.ATTESTED_AT, Timestamp.valueOf(dateTime) )
            .set(ATTESTATION_INSTANCE.COMMENTS, comments)
            .where(ATTESTATION_INSTANCE.ID.eq(instanceId).and(ATTESTATION_INSTANCE.ATTESTED_AT.isNull()))
            .execute() == 1;
    }

}
