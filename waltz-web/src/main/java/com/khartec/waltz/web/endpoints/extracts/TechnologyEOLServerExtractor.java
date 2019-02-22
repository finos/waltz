package com.khartec.waltz.web.endpoints.extracts;

import org.jooq.*;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class TechnologyEOLServerExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyEOLServerExtractor.class);

    public TechnologyEOLServerExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "technology-server", ":id");
        get(path, (request, response) -> {
            long ouId = getId(request);

            String orgUnitName = dsl
                    .select(ORGANISATIONAL_UNIT.NAME)
                    .from(ORGANISATIONAL_UNIT)
                    .where(ORGANISATIONAL_UNIT.ID.eq(ouId))
                    .fetchOne(ORGANISATIONAL_UNIT.NAME);

            checkNotNull(orgUnitName, "org unit cannot be null");
            String suggestedFilename = orgUnitName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-")
                    + "-technology-server-eol"
                    + ".csv";

            return writeFile(
                    suggestedFilename,
                    extract(ouId),
                    response);
        });
    }


    public CSVSerializer extract(long ouId) {
        return csvWriter -> {
            csvWriter.writeHeader(
                    "Org Unit",
                    "Application Name",
                    "Host Name",
                    "Environment",
                    "Operating System",
                    "Operating System EOL",
                    "Hardware EOL",
                    "Lifecycle");

            SelectConditionStep<Record1<Long>> ids = dsl.select(ENTITY_HIERARCHY.ID)
                    .from(ENTITY_HIERARCHY)
                    .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(ouId))
                    .and(ENTITY_HIERARCHY.KIND.eq("ORG_UNIT"));

            Select<Record> qry = dsl
                    .selectDistinct(ORGANISATIONAL_UNIT.NAME)
                    .select(APPLICATION.NAME)
                    .select(SERVER_INFORMATION.HOSTNAME,
                            SERVER_INFORMATION.ENVIRONMENT,
                            SERVER_INFORMATION.OPERATING_SYSTEM,
                            SERVER_INFORMATION.OS_END_OF_LIFE_DATE,
                            SERVER_INFORMATION.HW_END_OF_LIFE_DATE,
                            SERVER_INFORMATION.LIFECYCLE_STATUS)
                    .from(SERVER_INFORMATION)
                    .join(SERVER_USAGE)
                        .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                    .join(APPLICATION)
                        .on(APPLICATION.ID.eq(SERVER_USAGE.ENTITY_ID))
                    .join(ORGANISATIONAL_UNIT)
                        .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                    .where(ORGANISATIONAL_UNIT.ID.in(ids))
                    .and(APPLICATION.LIFECYCLE_PHASE.notEqual("RETIRED"));

            qry.fetch()
                    .forEach(r -> {
                        try {
                            csvWriter.write(
                                    r.get(ORGANISATIONAL_UNIT.NAME),
                                    r.get(APPLICATION.NAME),
                                    r.get(SERVER_INFORMATION.HOSTNAME),
                                    r.get(SERVER_INFORMATION.ENVIRONMENT),
                                    r.get(SERVER_INFORMATION.OPERATING_SYSTEM),
                                    r.get(SERVER_INFORMATION.OS_END_OF_LIFE_DATE),
                                    r.get(SERVER_INFORMATION.HW_END_OF_LIFE_DATE),
                                    r.get(SERVER_INFORMATION.LIFECYCLE_STATUS)
                            );
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write row: " + r, ioe);
                        }
                    });
        };
    }
}
