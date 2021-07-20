package com.khartec.waltz.web.endpoints.extracts;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.schema.tables.Application;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectHavingStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.AuthoritativeSource.AUTHORITATIVE_SOURCE;
import static com.khartec.waltz.schema.tables.DataType.DATA_TYPE;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;

@Service
public class AuthoritativeSourceExtractor extends DirectQueryBasedDataExtractor{

    public final static Application SUPPLIER_APP = Application.APPLICATION.as("supplier");


    @Autowired
    public AuthoritativeSourceExtractor(DSLContext dsl) {
        super(dsl);
    }


    @Override
    public void register() {
        get(mkPath("data-extract", "authoritative-source", "all"), (request, response) -> {
            SelectHavingStep <Record> qry = prepareExtractQuery(DSL.trueCondition());
            return writeExtract("authoritative-sources", qry, request, response);
        });
    }


    private SelectHavingStep<Record> prepareExtractQuery(Condition condition) {

        return dsl
                .select(SUPPLIER_APP.ID.as("Application Id"), SUPPLIER_APP.ASSET_CODE.as("Asset Code"), SUPPLIER_APP.NAME.as("Application Name"))
                .select(DATA_TYPE.NAME.as("Data Type"), DATA_TYPE.CODE.as("Data Type Code"))
                .select(AUTHORITATIVE_SOURCE.RATING.as("Rating Code"))
                .select(ENUM_VALUE.DISPLAY_NAME.as("Rating Name"))
                .select(AUTHORITATIVE_SOURCE.PARENT_KIND.as("Scope Entity Kind"))
                .select(DSL.coalesce(ORGANISATIONAL_UNIT.NAME, APPLICATION.NAME, ACTOR.NAME).as("Scope Entity Name"))
                .select(DSL.coalesce(ORGANISATIONAL_UNIT.EXTERNAL_ID, APPLICATION.ASSET_CODE, ACTOR.EXTERNAL_ID).as("Scope Entity External Id"))
                .select(AUTHORITATIVE_SOURCE.EXTERNAL_ID.as("Statement External Id"),
                        AUTHORITATIVE_SOURCE.LAST_UPDATED_AT.as("Statement Last Updated At"),
                        AUTHORITATIVE_SOURCE.LAST_UPDATED_BY.as("Statement Last Updated By"),
                        AUTHORITATIVE_SOURCE.PROVENANCE.as("Statement Provenance"))
                .from(AUTHORITATIVE_SOURCE)
                .innerJoin(SUPPLIER_APP).on(SUPPLIER_APP.ID.eq(AUTHORITATIVE_SOURCE.APPLICATION_ID))
                .innerJoin(DATA_TYPE).on(DATA_TYPE.CODE.eq(AUTHORITATIVE_SOURCE.DATA_TYPE))
                .innerJoin(ENUM_VALUE).on(ENUM_VALUE.TYPE.eq("AuthoritativenessRating")).and(ENUM_VALUE.KEY.eq(AUTHORITATIVE_SOURCE.RATING))
                .leftJoin(ORGANISATIONAL_UNIT).on(ORGANISATIONAL_UNIT.ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID)).and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ORG_UNIT.name()))
                .leftJoin(APPLICATION).on(APPLICATION.ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID)).and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.APPLICATION.name()))
                .leftJoin(ACTOR).on(ACTOR.ID.eq(AUTHORITATIVE_SOURCE.PARENT_ID)).and(AUTHORITATIVE_SOURCE.PARENT_KIND.eq(EntityKind.ACTOR.name()))
                .where(condition);
    }

}
