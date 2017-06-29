package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.data.EntityNameUtilities;
import com.khartec.waltz.data.logical_flow.LogicalFlowIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import com.khartec.waltz.schema.tables.OrganisationalUnit;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.DATA_TYPE;
import static com.khartec.waltz.schema.Tables.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.OrganisationalUnit.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;


@Service
public class LogicalFlowExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(LogicalFlowExtractor.class);

    private static final Field<String> SOURCE_NAME_FIELD = EntityNameUtilities.mkEntityNameField(
            LOGICAL_FLOW.SOURCE_ENTITY_ID,
            LOGICAL_FLOW.SOURCE_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

    private static final Field<String> TARGET_NAME_FIELD = EntityNameUtilities.mkEntityNameField(
            LOGICAL_FLOW.TARGET_ENTITY_ID,
            LOGICAL_FLOW.TARGET_ENTITY_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

    private LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory;


    @Autowired
    public LogicalFlowExtractor(DSLContext dsl,
                                LogicalFlowIdSelectorFactory logicalFlowIdSelectorFactory) {
        super(dsl);
        checkNotNull(logicalFlowIdSelectorFactory, "logicalFlowIdSelectorFactory cannot be null");

        this.logicalFlowIdSelectorFactory = logicalFlowIdSelectorFactory;
    }


    @Override
    public void register(String baseUrl) {
        post(mkPath(baseUrl, "logical-flows"), (request, response) -> {
            IdSelectionOptions options = readIdSelectionOptionsFromBody(request);
            CSVSerializer serializer = extract(options);
            return writeFile("logical-flows.csv", serializer, response);
        });
    }


    private CSVSerializer extract(IdSelectionOptions options) {

        Select<Record1<Long>> flowIdSelector = logicalFlowIdSelectorFactory.apply(options);

        Application sourceApp = APPLICATION.as("sourceApp");
        Application targetApp = APPLICATION.as("targetApp");
        OrganisationalUnit sourceOrgUnit = ORGANISATIONAL_UNIT.as("sourceOrgUnit");
        OrganisationalUnit targetOrgUnit = ORGANISATIONAL_UNIT.as("targetOrgUnit");

        Result<Record> data = dsl
                .select(SOURCE_NAME_FIELD, TARGET_NAME_FIELD)
                .select(sourceApp.ASSET_CODE)
                .select(targetApp.ASSET_CODE)
                .select(sourceOrgUnit.NAME)
                .select(targetOrgUnit.NAME)
                .select(DATA_TYPE.NAME)
                .select(LOGICAL_FLOW_DECORATOR.RATING)
                .from(LOGICAL_FLOW)
                .leftJoin(sourceApp).on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(sourceApp.ID).and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(targetApp).on(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(targetApp.ID).and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .leftJoin(sourceOrgUnit).on(sourceApp.ORGANISATIONAL_UNIT_ID.eq(sourceOrgUnit.ID))
                .leftJoin(targetOrgUnit).on(targetApp.ORGANISATIONAL_UNIT_ID.eq(targetOrgUnit.ID))
                .join(LOGICAL_FLOW_DECORATOR).on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID).and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq("DATA_TYPE")))
                .join(DATA_TYPE).on(DATA_TYPE.ID.eq(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID).and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq("DATA_TYPE")))
                .where(LOGICAL_FLOW.ID.in(flowIdSelector))
                .and(LOGICAL_FLOW.IS_REMOVED.isFalse())
                .fetch();

        CSVSerializer serializer = csvWriter -> {
            csvWriter.writeHeader(
                    "Source",
                    "Source NAR",
                    "Source Org Unit",
                    "Target",
                    "Target NAR",
                    "Target Org Unit",
                    "Data Type",
                    "Source Rating");

            data.forEach(r -> {
                try {
                    csvWriter.write(
                            r.get(SOURCE_NAME_FIELD),
                            r.get(sourceApp.ASSET_CODE),
                            r.get(sourceOrgUnit.NAME),
                            r.get(TARGET_NAME_FIELD),
                            r.get(targetApp.ASSET_CODE),
                            r.get(targetOrgUnit.NAME),
                            r.get(DATA_TYPE.NAME),
                            r.get(LOGICAL_FLOW_DECORATOR.RATING));
                } catch (IOException ioe) {
                    LOG.warn("Failed to write logical flow: " + r, ioe);
                }
            });
        };

        return serializer;
    }

}
