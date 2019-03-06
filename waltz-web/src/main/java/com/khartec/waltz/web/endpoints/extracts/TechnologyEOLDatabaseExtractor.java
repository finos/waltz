package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.EntityReference;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spark.Request;


import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.application.ApplicationIdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getEntityReference;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class TechnologyEOLDatabaseExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyEOLDatabaseExtractor.class);
    private final EntityReferenceNameResolver nameResolver;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;

    @Autowired
    public TechnologyEOLDatabaseExtractor(DSLContext dsl,
                                          EntityReferenceNameResolver nameResolver,
                                          ApplicationIdSelectorFactory applicationIdSelectorFactory) {
        super(dsl);
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(applicationIdSelectorFactory, "appIdSelectorFactory cannot be null");
        this.nameResolver = nameResolver;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
    }


    @Override
    public void register() {
        String path = mkPath("data-extract", "technology-database", ":kind", ":id");
        get(path, (request, response) -> {
            EntityReference ref = getReference(request);
            Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(mkOpts(ref));

            String data = dsl
                    .selectDistinct(ORGANISATIONAL_UNIT.NAME.as("Org Unit"))
                    .select(APPLICATION.NAME.as("Application Name"))
                    .select(DATABASE_INFORMATION.DATABASE_NAME.as("Database Name"),
                            DATABASE_INFORMATION.INSTANCE_NAME.as("Instance Name"),
                            DATABASE_INFORMATION.ENVIRONMENT.as("DBMS Environment"),
                            DATABASE_INFORMATION.DBMS_VENDOR.as("DBMS Vendor"),
                            DATABASE_INFORMATION.DBMS_NAME.as("DBMS Name"),
                            DATABASE_INFORMATION.END_OF_LIFE_DATE.as("End of Life date"),
                            DATABASE_INFORMATION.LIFECYCLE_STATUS.as("Lifecycle"))
                    .from(DATABASE_INFORMATION)
                    .join(APPLICATION)
                    .on(APPLICATION.ASSET_CODE.eq(DATABASE_INFORMATION.ASSET_CODE))
                    .join(ORGANISATIONAL_UNIT)
                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                    .where(APPLICATION.ID.in(appIdSelector))
                    .and(APPLICATION.LIFECYCLE_PHASE.notEqual("RETIRED"))
                    .fetch()
                    .formatCSV();

            return writeFile(
                    mkFilename(ref),
                    data,
                    response);
        });
    }


    private EntityReference getReference(Request request) {
        EntityReference origRef = getEntityReference(request);
        return nameResolver
                .resolve(origRef)
                .orElse(origRef);
    }


    private String mkFilename(EntityReference ref) {
        return sanitizeName(ref.name().orElse(ref.kind().name()))
                + "-technology-database-eol"
                + ".csv";
    }


    private String sanitizeName(String str) {
        return str
                .replace(".", "-")
                .replace(" ", "-")
                .replace(",", "-");

    }
}
