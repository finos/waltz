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

package com.khartec.waltz.data.attestation;

import com.khartec.waltz.schema.tables.records.AttestationInstanceRecipientRecord;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
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


    public List<String> findRecipientsByRunId(Long id) {

        return dsl
                .select(ATTESTATION_INSTANCE_RECIPIENT.USER_ID)
                .from(ATTESTATION_INSTANCE_RECIPIENT)
                .innerJoin(ATTESTATION_INSTANCE)
                .on(ATTESTATION_INSTANCE.ID.eq(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID))
                .where(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(id))
                .fetch(ATTESTATION_INSTANCE_RECIPIENT.USER_ID);
    }
}
