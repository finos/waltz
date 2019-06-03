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
public class TechnologyEOLServerExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyEOLServerExtractor.class);
    private final EntityReferenceNameResolver nameResolver;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;


    @Autowired
    public TechnologyEOLServerExtractor(DSLContext dsl,
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
        String path = mkPath("data-extract", "technology-server", ":kind", ":id");
        get(path, (request, response) -> {
            EntityReference ref = getReference(request);
            Select<Record1<Long>> appIdSelector = applicationIdSelectorFactory.apply(mkOpts(ref));

            SelectConditionStep<Record> qry = dsl
                    .selectDistinct(ORGANISATIONAL_UNIT.NAME.as("Org Unit"))
                    .select(APPLICATION.NAME.as("Application Name"), APPLICATION.ASSET_CODE.as("Asset Code"))
                    .select(SERVER_INFORMATION.HOSTNAME.as("Host Name"),
                            SERVER_USAGE.ENVIRONMENT.as("Environment"),
                            SERVER_INFORMATION.OPERATING_SYSTEM.as("Operating System"),
                            SERVER_INFORMATION.OS_END_OF_LIFE_DATE.as("Operating System EOL"),
                            SERVER_INFORMATION.HW_END_OF_LIFE_DATE.as("Hardware EOL"),
                            SERVER_INFORMATION.LIFECYCLE_STATUS.as("Lifecycle"))
                    .from(SERVER_INFORMATION)
                    .join(SERVER_USAGE)
                    .on(SERVER_USAGE.SERVER_ID.eq(SERVER_INFORMATION.ID))
                    .join(APPLICATION)
                    .on(APPLICATION.ID.eq(SERVER_USAGE.ENTITY_ID))
                    .join(ORGANISATIONAL_UNIT)
                    .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                    .where(APPLICATION.ID.in(appIdSelector))
                    .and(APPLICATION.LIFECYCLE_PHASE.notEqual("RETIRED"));

            return writeExtract(
                    mkFilename(ref),
                    qry,
                    request,
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
                        + "-technology-server-eol";
    }


    private String sanitizeName(String str) {
        return str
                .replace(".", "-")
                .replace(" ", "-")
                .replace(",", "-");
    }
}
