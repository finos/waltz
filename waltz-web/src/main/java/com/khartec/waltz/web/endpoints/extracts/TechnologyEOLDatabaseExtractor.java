package com.khartec.waltz.web.endpoints.extracts;

import org.jooq.*;
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
public class TechnologyEOLDatabaseExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyEOLDatabaseExtractor.class);

    public TechnologyEOLDatabaseExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "technology-database", ":id");
        get(path, (request, response) -> {
            long runId = getId(request);

            String orgUnitName = dsl
                    .select(ORGANISATIONAL_UNIT.NAME)
                    .from(ORGANISATIONAL_UNIT)
                    .where(ORGANISATIONAL_UNIT.ID.eq(runId))
                    .fetchOne(ORGANISATIONAL_UNIT.NAME);

            checkNotNull(orgUnitName, "org unit cannot be null");
            String suggestedFilename = orgUnitName
                    .replace(".", "-")
                    .replace(" ", "-")
                    .replace(",", "-")
                    + "-technology-database-eol"
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
                    "Org Unit",
                    "Application Name",
                    "Database Name",
                    "Instance Name",
                    "DBMS Environment",
                    "DBMS Vendor",
                    "DBMS Name",
                    "End of life date",
                    "Lifecycle");

            SelectConditionStep<Record1<Long>> ids = dsl.select(ENTITY_HIERARCHY.ID)
                    .from(ENTITY_HIERARCHY)
                    .where(ENTITY_HIERARCHY.ANCESTOR_ID.eq(runId))
                    .and(ENTITY_HIERARCHY.KIND.eq("ORG_UNIT"));

            Select<Record> qry = dsl
                    .selectDistinct(ORGANISATIONAL_UNIT.NAME)
                    .select(APPLICATION.NAME)
                    .select(DATABASE_INFORMATION.DATABASE_NAME,
                            DATABASE_INFORMATION.INSTANCE_NAME,
                            DATABASE_INFORMATION.ENVIRONMENT,
                            DATABASE_INFORMATION.DBMS_VENDOR,
                            DATABASE_INFORMATION.DBMS_NAME,
                            DATABASE_INFORMATION.END_OF_LIFE_DATE,
                            DATABASE_INFORMATION.LIFECYCLE_STATUS)
                    .from(DATABASE_INFORMATION)
                    .join(APPLICATION)
                        .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                    .join(ENTITY_HIERARCHY)
                        .on(ENTITY_HIERARCHY.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID).and(ENTITY_HIERARCHY.KIND.eq("ORG_UNIT")))
                    .join(ORGANISATIONAL_UNIT)
                        .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                    .where(APPLICATION.LIFECYCLE_PHASE.notEqual("RETIRED"))
                        .and(ORGANISATIONAL_UNIT.ID.in(ids)) ;

            qry.fetch()
                    .forEach(r -> {
                        try {
                            csvWriter.write(
                                    r.get(ORGANISATIONAL_UNIT.NAME),
                                    r.get(APPLICATION.NAME),
                                    r.get(DATABASE_INFORMATION.DATABASE_NAME),
                                    r.get(DATABASE_INFORMATION.INSTANCE_NAME),
                                    r.get(DATABASE_INFORMATION.ENVIRONMENT),
                                    r.get(DATABASE_INFORMATION.DBMS_VENDOR),
                                    r.get(DATABASE_INFORMATION.DBMS_NAME),
                                    r.get(DATABASE_INFORMATION.END_OF_LIFE_DATE),
                                    r.get(DATABASE_INFORMATION.LIFECYCLE_STATUS)
                            );
                        } catch (IOException ioe) {
                            LOG.warn("Failed to write row: " + r, ioe);
                        }
                    });
        };
    }
}
