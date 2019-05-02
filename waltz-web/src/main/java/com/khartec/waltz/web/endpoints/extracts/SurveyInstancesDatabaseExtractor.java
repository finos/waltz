package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.model.EntityKind;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_RUN;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.getId;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static spark.Spark.get;


@Service
public class SurveyInstancesDatabaseExtractor extends BaseDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyInstancesDatabaseExtractor.class);

    @Autowired
    public SurveyInstancesDatabaseExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        String path = mkPath("data-extract", "survey-instances", ":id");

        get(path, (request, response) -> {
            long surveyTemplateId = getId(request);

            String data = dsl
                    .select(APPLICATION.NAME.as("Application Name"),
                            APPLICATION.ASSET_CODE.as("Asset Code"),
                            SURVEY_INSTANCE.STATUS.as("Status"),
                            SURVEY_INSTANCE.DUE_DATE.as("Due Date"))
                    .from(SURVEY_INSTANCE)
                    .join(APPLICATION)
                    .on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()).and(APPLICATION.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                    .join(SURVEY_RUN)
                    .on(SURVEY_RUN.ID.eq(SURVEY_INSTANCE.SURVEY_RUN_ID))
                    .where(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(surveyTemplateId))
                    .fetch()
                    .formatCSV();

            LOG.info("Survey Instances CSV has been exported successfully");
            return writeFile(
                    mkFilename(surveyTemplateId),
                    data,
                    response);
        });
    }

    private String mkFilename(long id) {
        return "survey-instances-"
                + id
                + ".csv";
    }
}
