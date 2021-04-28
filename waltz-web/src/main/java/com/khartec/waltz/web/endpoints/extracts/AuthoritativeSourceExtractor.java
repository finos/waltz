package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.LOGICAL_FLOW;
import static com.khartec.waltz.schema.Tables.ORGANISATIONAL_UNIT;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.WebUtilities.readIdSelectionOptionsFromBody;
import static spark.Spark.post;

@Service
public class AuthoritativeSourceExtractor extends DirectQueryBasedDataExtractor{

    private static final Logger LOG = LoggerFactory.getLogger(AuthoritativeSourceExtractor.class);
    private final OrganisationalUnitIdSelectorFactory organisationalUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();
    public final static Application CONSUMER_APP = Application.APPLICATION.as("consumer");
    public final static Application SUPPLIER_APP = Application.APPLICATION.as("supplier");


    @Autowired
    public AuthoritativeSourceExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        post(mkPath("data-extract", "authoritative-source", "by-selector"), (request, response) -> {
            IdSelectionOptions idSelectionOptions = readIdSelectionOptionsFromBody(request);
            Select<Record1<Long>> orgUnitSelector = organisationalUnitIdSelectorFactory.apply(idSelectionOptions);
            Condition condition =
                    AUTHORITATIVE_SOURCE.ID.in(
                      dsl.select(AUTHORITATIVE_SOURCE.ID)
                        .from(AUTHORITATIVE_SOURCE)
                        .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID)
                          .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                          .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.ne(EntityLifecycleStatus.REMOVED.name())))
                        .innerJoin(CONSUMER_APP).on(CONSUMER_APP.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID)
                           .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                      .where(AUTHORITATIVE_SOURCE.PARENT_ID.in(orgUnitSelector)));

            SelectHavingStep <Record> qry = prepareExtractQuery(condition);
            String fileName = String.format("authoritative-sources-for-%s-%s",
                    idSelectionOptions.entityReference().kind().name().toLowerCase(),
                    idSelectionOptions.entityReference().id());
            LOG.debug("extracted authoritative sources for entity ref {}", idSelectionOptions.entityReference());
            return writeExtract(fileName, qry, request, response);
        });
    }


    private SelectHavingStep<Record> prepareExtractQuery(Condition condition) {

        return dsl
                .select(ORGANISATIONAL_UNIT.NAME.as("Org Name"))
                .select(SUPPLIER_APP.ID.as("Application Id"), SUPPLIER_APP.ASSET_CODE.as("Asset Code"), SUPPLIER_APP.NAME.as("Application Name"))
                .select(DATA_TYPE.NAME.as("Data Type"))
                .select(AUTHORITATIVE_SOURCE.RATING.as("Rating"))
                .from(AUTHORITATIVE_SOURCE)
                .innerJoin(SUPPLIER_APP).on(SUPPLIER_APP.ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID))
                .innerJoin(ORGANISATIONAL_UNIT).on(ORGANISATIONAL_UNIT.ID.eq(SUPPLIER_APP.ORGANISATIONAL_UNIT_ID))
                .innerJoin(DATA_TYPE).on(DATA_TYPE.CODE.eq(AUTHORITATIVE_SOURCE.DATA_TYPE))
                .where(condition)
                .groupBy(ORGANISATIONAL_UNIT.NAME, SUPPLIER_APP.ID, SUPPLIER_APP.ASSET_CODE, SUPPLIER_APP.NAME,DATA_TYPE.NAME, AUTHORITATIVE_SOURCE.RATING);
    }

}
