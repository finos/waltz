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

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Operation;
import com.khartec.waltz.schema.tables.ChangeLog;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

import static com.khartec.waltz.common.ListUtilities.asList;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.web.WebUtilities.*;
import static java.lang.String.format;
import static spark.Spark.get;
import static spark.Spark.post;


@Service
public class ChangeLogExtractor extends DirectQueryBasedDataExtractor {

    @Autowired
    public ChangeLogExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        registerExtractForApp(mkPath("data-extract", "change-log", ":kind", ":id"));
        registerExtractUnattestedChangesForApp(mkPath("data-extract", "change-log", "unattested-changes", ":childKind", ":kind", ":id"));
    }


    private void registerExtractForApp(String path) {
        post(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);

            Select<Record> select = mkQuery(entityRef);

            SelectSeekStep1<Record4<String, String, String, Timestamp>, Timestamp> qry = dsl
                    .select(select.field(CHANGE_LOG.SEVERITY).as("Severity"),
                            select.field(CHANGE_LOG.MESSAGE).as("Message"),
                            select.field(CHANGE_LOG.USER_ID).as("User"),
                            select.field(CHANGE_LOG.CREATED_AT).as("Timestamp"))
                    .from(select)
                    .orderBy(select.field(CHANGE_LOG.CREATED_AT).desc());

            return writeExtract(
                    "change-log-" + entityRef.id(),
                    qry,
                    request,
                    response);
        });
    }


    private Select<Record> mkQuery(EntityReference entityRef) {
        Select<Record> byParentRef = DSL
                .select(CHANGE_LOG.fields())
                .from(ChangeLog.CHANGE_LOG)
                .where(ChangeLog.CHANGE_LOG.PARENT_ID.eq(entityRef.id()))
                .and(ChangeLog.CHANGE_LOG.PARENT_KIND.eq(entityRef.kind().name()));

        switch (entityRef.kind()) {
            case PERSON:
                SelectConditionStep<Record> byUserId = DSL
                        .select(CHANGE_LOG.fields())
                        .from(ChangeLog.CHANGE_LOG)
                        .innerJoin(PERSON).on(PERSON.EMAIL.eq(ChangeLog.CHANGE_LOG.USER_ID))
                        .where(PERSON.ID.eq(entityRef.id()));
                return byParentRef.unionAll(byUserId);
            default:
                return byParentRef;
        }
    }


    private void registerExtractUnattestedChangesForApp(String path) {
        get(path, (request, response) -> {

            EntityReference entityRef = getEntityReference(request);
            EntityKind childKind = getKind(request, "childKind");

            SelectSeekStep1<Record4<String, String, String, Timestamp>, Timestamp> qry = mkUnattestedChangesQuery(entityRef, childKind);

            String filename = format("unattested-changes-%s-%d", childKind, entityRef.id());

            return writeExtract(
                    filename,
                    qry,
                    request,
                    response);
        });
    }


    private SelectSeekStep1<Record4<String, String, String, Timestamp>, Timestamp> mkUnattestedChangesQuery(EntityReference entityRef, EntityKind childKind) {

        List<String> operationKinds = asList(
                Operation.UPDATE.name(),
                Operation.ADD.name(),
                Operation.REMOVE.name());

        //find latest attestation
        Field<Timestamp> latestAttestationTime = DSL.max(ATTESTATION_INSTANCE.ATTESTED_AT).as("latestAttestationTime");

        Table<Record4<String, Long, String, Timestamp>> latestAttestation = DSL
                .select(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND,
                        ATTESTATION_INSTANCE.PARENT_ENTITY_ID,
                        ATTESTATION_INSTANCE.ATTESTED_ENTITY_KIND,
                        latestAttestationTime)
                .from(ATTESTATION_INSTANCE)
                .where(ATTESTATION_INSTANCE.PARENT_ENTITY_ID.eq(entityRef.id()))
                .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(entityRef.kind().name()))
                .and(ATTESTATION_INSTANCE.ATTESTED_AT.isNotNull())
                .and(ATTESTATION_INSTANCE.ATTESTED_ENTITY_KIND.eq(childKind.name()))
                .groupBy(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND,
                        ATTESTATION_INSTANCE.PARENT_ENTITY_ID,
                        ATTESTATION_INSTANCE.ATTESTED_ENTITY_KIND)
                .asTable("latest_attestation");

        //only returns changes that apply to that child kind
        return dsl
                .select(CHANGE_LOG.SEVERITY.as("Severity"),
                        CHANGE_LOG.MESSAGE.as("Message"),
                        CHANGE_LOG.USER_ID.as("User"),
                        CHANGE_LOG.CREATED_AT.as("Timestamp"))
                .from(CHANGE_LOG)
                .innerJoin(latestAttestation)
                .on(latestAttestation.field(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND).eq(CHANGE_LOG.PARENT_KIND)
                        .and(latestAttestation.field(ATTESTATION_INSTANCE.PARENT_ENTITY_ID).eq(CHANGE_LOG.PARENT_ID)
                                .and(latestAttestation.field(ATTESTATION_INSTANCE.ATTESTED_ENTITY_KIND).eq(CHANGE_LOG.CHILD_KIND))))
                .where(CHANGE_LOG.OPERATION.in(operationKinds))
                .and(CHANGE_LOG.CREATED_AT.gt(latestAttestation.field(latestAttestationTime)))
                .orderBy(CHANGE_LOG.CREATED_AT.desc());
    }

}
