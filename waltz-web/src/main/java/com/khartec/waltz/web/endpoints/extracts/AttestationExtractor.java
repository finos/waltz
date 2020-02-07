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

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.AttestationInstance;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static com.khartec.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static com.khartec.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;
import static com.khartec.waltz.web.WebUtilities.*;
import static spark.Spark.get;
import static spark.Spark.post;


@Service
public class AttestationExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(AttestationExtractor.class);
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();

    public AttestationExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {

        registerExtractForRun(mkPath("data-extract", "attestation", ":id"));
        registerExtractForFlowTypeAndSelector(mkPath("data-extract", "attestations", ":kind"));

    }


    private void registerExtractForFlowTypeAndSelector(String path) {

        post(path, (request, response) -> {

            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> appIds = applicationIdSelectorFactory.apply(idSelectionOptions);
            EntityKind kind = getKind(request);

            AttestationInstance attestationInstance = ATTESTATION_INSTANCE.as("attestationInstance");
            AttestationInstance attestationInstance2 = ATTESTATION_INSTANCE.as("attestationInstance2");

            String fileName = String.format("attestations-for-%s-%s-%s",
                    idSelectionOptions.entityReference().kind().name().toLowerCase(),
                    idSelectionOptions.entityReference().id(),
                    kind.name().toLowerCase());

            SelectHavingStep<Record2<Long, Timestamp>> latestAttestation = dsl
                    .selectDistinct(attestationInstance.PARENT_ENTITY_ID.as("parent_id"),
                            DSL.max(attestationInstance.ATTESTED_AT).as("latest_attested_at"))
                    .from(attestationInstance)
                    .innerJoin(ATTESTATION_RUN).on(attestationInstance.ATTESTATION_RUN_ID.eq(ATTESTATION_RUN.ID))
                    .where(attestationInstance.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                    .and(ATTESTATION_RUN.ATTESTED_ENTITY_KIND.eq(kind.name())))
                    .groupBy(attestationInstance.PARENT_ENTITY_ID);

            SelectOnConditionStep<Record3<String, Timestamp, Long>> peopleToAttest = dsl
                    .select(attestationInstance2.ATTESTED_BY,
                            attestationInstance2.ATTESTED_AT,
                            attestationInstance2.PARENT_ENTITY_ID)
                    .from(attestationInstance2)
                    .innerJoin(latestAttestation).on(attestationInstance2.PARENT_ENTITY_ID.eq(latestAttestation.field("parent_id").coerce(attestationInstance2.PARENT_ENTITY_ID)))
                    .and(attestationInstance2.ATTESTED_AT.eq(latestAttestation.field("latest_attested_at").coerce(attestationInstance2.ATTESTED_AT)));

            SelectConditionStep<Record> qry = dsl
                    .select(APPLICATION.NAME.as("Name"),
                            APPLICATION.ASSET_CODE.as("Asset Code"),
                            APPLICATION.KIND.as("Kind"),
                            APPLICATION.BUSINESS_CRITICALITY.as("Business Criticality"),
                            APPLICATION.LIFECYCLE_PHASE.as("Lifecycle Phase"))
                    .select(peopleToAttest.field(attestationInstance2.ATTESTED_BY).as("Last Attested By"),
                            peopleToAttest.field(attestationInstance2.ATTESTED_AT).as("Last Attested At"))
                    .from(APPLICATION)
                    .leftJoin(peopleToAttest).on(APPLICATION.ID.eq(peopleToAttest.field(attestationInstance2.PARENT_ENTITY_ID)))
                    .where(APPLICATION.ID.in(appIds));

            return writeExtract(
                    fileName,
                    qry,
                    request,
                    response);
        });

    }


    private void registerExtractForRun(String path) {

        get(path, (request, response) -> {
            long runId = getId(request);

            String runName = dsl
                    .select(ATTESTATION_RUN.NAME)
                    .from(ATTESTATION_RUN)
                    .where(ATTESTATION_RUN.ID.eq(runId))
                    .fetchOne(ATTESTATION_RUN.NAME);

            checkNotNull(runName, "AttestationRun cannot be null");
            String suggestedFilename = runName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-");

            return writeExtract(
                    suggestedFilename,
                    prepareExtractQuery(runId),
                    request,
                    response);
        });
    }


    private SelectConditionStep<Record> prepareExtractQuery(long runId) {
        return dsl
                .select(APPLICATION.NAME.as("Application"),
                        APPLICATION.ASSET_CODE.as("External Id"))
                .select(ATTESTATION_RUN.ATTESTED_ENTITY_KIND.as("Attesting Kind"),
                        ATTESTATION_RUN.ATTESTED_ENTITY_ID.as("Attesting Kind Id"))
                .select(ATTESTATION_INSTANCE.ID.as("Attestation Id"),
                        ATTESTATION_INSTANCE.ATTESTED_BY.as("Attested By"),
                        ATTESTATION_INSTANCE.ATTESTED_AT.as("Attested At"))
                .select(ATTESTATION_INSTANCE_RECIPIENT.USER_ID.as("Recipient"))
                .from(ATTESTATION_INSTANCE)
                .join(ATTESTATION_INSTANCE_RECIPIENT)
                    .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                .join(ATTESTATION_RUN)
                    .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                .join(APPLICATION)
                    .on(APPLICATION.ID.eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID))
                .where(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(runId))
                    .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));
    }
}
