package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
                    .replace(",", "-")
                    + ".csv";

            return writeFile(
                    suggestedFilename,
                    extract(runId),
                    response);
        });
    }


    private CSVSerializer extract(long runId) {
        return csvWriter -> {
            csvWriter.writeHeader(
                    "Application",
                    "External Id",
                    "Attesting Kind",
                    "Attesting Kind Id",
                    "Attestation Id",
                    "Attested By",
                    "Attested At",
                    "Recipient");

            Select<Record> qry = dsl
                    .select(ATTESTATION_INSTANCE.fields())
                    .select(ATTESTATION_INSTANCE_RECIPIENT.USER_ID)
                    .select(ATTESTATION_RUN.ATTESTED_ENTITY_KIND, ATTESTATION_RUN.ATTESTED_ENTITY_ID)
                    .select(APPLICATION.NAME, APPLICATION.ASSET_CODE)
                    .from(ATTESTATION_INSTANCE)
                    .join(ATTESTATION_INSTANCE_RECIPIENT)
                        .on(ATTESTATION_INSTANCE_RECIPIENT.ATTESTATION_INSTANCE_ID.eq(ATTESTATION_INSTANCE.ID))
                    .join(ATTESTATION_RUN)
                        .on(ATTESTATION_RUN.ID.eq(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID))
                    .join(APPLICATION)
                        .on(APPLICATION.ID.eq(ATTESTATION_INSTANCE.PARENT_ENTITY_ID))
                    .where(ATTESTATION_INSTANCE.ATTESTATION_RUN_ID.eq(runId))
                        .and(ATTESTATION_INSTANCE.PARENT_ENTITY_KIND.eq(EntityKind.APPLICATION.name()));

            qry.fetch()
                    .forEach(r -> {
                        try {
                            csvWriter.write(
                                    r.get(APPLICATION.NAME),
                                    r.get(APPLICATION.ASSET_CODE),
                                    r.get(ATTESTATION_RUN.ATTESTED_ENTITY_KIND),
                                    r.get(ATTESTATION_RUN.ATTESTED_ENTITY_ID),
                                    r.get(ATTESTATION_INSTANCE.ID),
                                    r.get(ATTESTATION_INSTANCE.ATTESTED_BY),
                                    r.get(ATTESTATION_INSTANCE.ATTESTED_AT),
                                    r.get(ATTESTATION_INSTANCE_RECIPIENT.USER_ID)
                            );
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write row: " + r, ioe);
                        }
                    });
        };
    }
}
