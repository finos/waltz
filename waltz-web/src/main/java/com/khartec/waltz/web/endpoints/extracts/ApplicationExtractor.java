package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.SelectorUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.APPLICATION;
import static com.khartec.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class ApplicationExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationExtractor.class);
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory = new ApplicationIdSelectorFactory();


    @Autowired
    public ApplicationExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        post(mkPath("data-extract", "application", "by-selector"), (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> idSelector = applicationIdSelectorFactory.apply(idSelectionOptions);
            Condition condition =
                    Application.APPLICATION.ID.in(idSelector)
                            .and(SelectorUtilities.mkApplicationConditions(idSelectionOptions));
            SelectConditionStep<?> qry = prepareExtractQuery(condition);
            String fileName = String.format("application-for-%s-%s",
                    idSelectionOptions.entityReference().kind().name().toLowerCase(),
                    idSelectionOptions.entityReference().id());
            LOG.debug("extracted applications for entity ref {}", idSelectionOptions.entityReference());
            return writeExtract(fileName, qry, request, response);
        });
    }


    private SelectConditionStep<Record8<Long, String, String, String, String, String, String, String>> prepareExtractQuery(Condition condition) {

        return dsl
                .selectDistinct(
                        APPLICATION.ID.as("Waltz Id"),
                        APPLICATION.NAME.as("Name"),
                        APPLICATION.ASSET_CODE.as("Asset Code"),
                        ORGANISATIONAL_UNIT.NAME.as("Org Unit"),
                        APPLICATION.KIND.as("Application Kind"),
                        APPLICATION.OVERALL_RATING.as("Overall Rating"),
                        APPLICATION.BUSINESS_CRITICALITY.as("Business Criticality"),
                        APPLICATION.LIFECYCLE_PHASE.as("Lifecycle Phase"))
                .from(APPLICATION)
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(ORGANISATIONAL_UNIT.ID.eq(APPLICATION.ORGANISATIONAL_UNIT_ID))
                .where(condition);

    }
}
