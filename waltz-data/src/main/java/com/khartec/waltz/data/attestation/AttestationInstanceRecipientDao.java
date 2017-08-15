package com.khartec.waltz.data.attestation;

import com.khartec.waltz.schema.tables.records.AttestationInstanceRecipientRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.khartec.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;

@Repository
public class AttestationInstanceRecipientDao {

    private final DSLContext dsl;


    @Autowired
    public AttestationInstanceRecipientDao(DSLContext dsl) {
        this.dsl = dsl;
    }


    public long create(long instanceId, String userId) {
        AttestationInstanceRecipientRecord record = dsl.newRecord(ATTESTATION_INSTANCE_RECIPIENT);

        record.setAttestationInstanceId(instanceId);
        record.setUserId(userId);

        record.store();

        return record.getId();
    }
}
