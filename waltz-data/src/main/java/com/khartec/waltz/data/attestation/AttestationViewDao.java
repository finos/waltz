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

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.schema.tables.AttestationInstance;
import com.khartec.waltz.schema.tables.ChangeLog;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

import static com.khartec.waltz.common.Checks.checkNotNull;


@Repository
public class AttestationViewDao {


    private final DSLContext dsl;


    @Autowired
    public AttestationViewDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");
        this.dsl = dsl;
    }


    public void foo(EntityReference ref) {
        ChangeLog cl = ChangeLog.CHANGE_LOG.as("cl");
        AttestationInstance ai = AttestationInstance.ATTESTATION_INSTANCE.as("ai");

        Condition changeLogEntryOfInterest = cl.OPERATION.in(
                Operation.UPDATE.name(),
                Operation.ADD.name(),
                Operation.REMOVE.name());

        Table<Record4<String, Long, String, Timestamp>> latestAttestationsSubQuery = DSL
                .select(ai.PARENT_ENTITY_KIND.as("pek"),
                        ai.PARENT_ENTITY_ID .as("pei"),
                        ai.ATTESTED_ENTITY_KIND.as("aek"),
                        DSL.max(ai.ATTESTED_AT).as("aat"))
                .from(ai)
                .where(ai.PARENT_ENTITY_ID.eq(ref.id()))
                .and(ai.PARENT_ENTITY_KIND.eq(ref.kind().name()))
                .and(ai.ATTESTED_AT.isNotNull())
                .groupBy(ai.PARENT_ENTITY_KIND, ai.PARENT_ENTITY_ID, ai.ATTESTED_ENTITY_KIND)
                .asTable("latest_attestation");

        Condition joinChangeLogToLatestAttestationCondition = latestAttestationsSubQuery.field("pek", String.class).eq(cl.PARENT_KIND)
                .and(latestAttestationsSubQuery.field("pei", Long.class).eq(cl.PARENT_ID))
                .and(latestAttestationsSubQuery.field("aek", String.class).eq(cl.CHILD_KIND));

        // a.ek, a.ei, a.aek, cl.message, cl.operation, cl.created_at, cl.user_id
        SelectConditionStep<Record7<String, Long, String, String, String, Timestamp, String>> qry = dsl
                .select(latestAttestationsSubQuery.field("pek", String.class),
                        latestAttestationsSubQuery.field("pei", Long.class),
                        latestAttestationsSubQuery.field("aek", String.class),
                        cl.OPERATION,
                        cl.MESSAGE,
                        cl.CREATED_AT,
                        cl.USER_ID)
                .from(cl)
                .innerJoin(latestAttestationsSubQuery)
                .on(joinChangeLogToLatestAttestationCondition)
                .where(cl.CREATED_AT.greaterThan(latestAttestationsSubQuery.field("aat", Timestamp.class)))
                .and(changeLogEntryOfInterest);

        System.out.println(qry);

        qry.fetch().formatCSV(System.out);
    }


}