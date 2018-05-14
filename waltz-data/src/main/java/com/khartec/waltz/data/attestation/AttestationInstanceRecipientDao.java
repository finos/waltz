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
                .where(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(id))
                .fetch(ATTESTATION_INSTANCE_RECIPIENT.USER_ID);
    }
}
