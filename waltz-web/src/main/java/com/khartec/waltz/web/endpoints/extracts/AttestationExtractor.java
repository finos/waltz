package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.AttestationInstance.ATTESTATION_INSTANCE;
import static com.khartec.waltz.schema.tables.AttestationInstanceRecipient.ATTESTATION_INSTANCE_RECIPIENT;
import static com.khartec.waltz.schema.tables.AttestationRun.ATTESTATION_RUN;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class AttestationExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(AttestationExtractor.class);

    public AttestationExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "attestation", ":id");
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
