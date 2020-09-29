/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.web.endpoints.extracts;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.GenericSelector;
import com.khartec.waltz.data.GenericSelectorFactory;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.web.WebUtilities.*;
import static java.lang.String.format;
import static spark.Spark.get;


@Service
public class SurveyRunExtractor extends DirectQueryBasedDataExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyRunExtractor.class);
    private static List<Field> SURVEY_RESPONSE_FIELDS;
    private static List<Field> SURVEY_INSTANCE_FIELDS;

    private final GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();


    static {
        Field<String> ENTITY_RESPONSE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_ID,
                SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_KIND);

        Field<String> answer = DSL.concat(SURVEY_QUESTION_RESPONSE.STRING_RESPONSE.coalesce(""),
                SURVEY_QUESTION_RESPONSE.NUMBER_RESPONSE.cast(String.class).coalesce(""),
                SURVEY_QUESTION_RESPONSE.BOOLEAN_RESPONSE.cast(String.class).coalesce(""),
                SURVEY_QUESTION_RESPONSE.DATE_RESPONSE.cast(String.class).coalesce(""),
                ENTITY_RESPONSE_NAME_FIELD.coalesce(""),
                SURVEY_QUESTION_RESPONSE.LIST_RESPONSE_CONCAT.coalesce("")).as("Answer");

        SURVEY_RESPONSE_FIELDS = ListUtilities.asList(
                SURVEY_QUESTION.SECTION_NAME.as("Section"),
                SURVEY_QUESTION.QUESTION_TEXT.as("Question"),
                answer,
                SURVEY_QUESTION_RESPONSE.COMMENT.as("Comment"));

        SURVEY_INSTANCE_FIELDS = ListUtilities.asList(
                SURVEY_INSTANCE.DUE_DATE.as("Due Date"),
                SURVEY_INSTANCE.SUBMITTED_BY.as("Submitted By"),
                SURVEY_INSTANCE.SUBMITTED_AT.as("Submitted At"),
                SURVEY_INSTANCE.APPROVED_BY.as("Approved By"),
                SURVEY_INSTANCE.APPROVED_AT.as("Approved At"));
    }

    @Autowired
    public SurveyRunExtractor(DSLContext dsl) {
        super(dsl);
    }

    @Override
    public void register() {
        String surveysForEntityPath = mkPath("data-extract", "surveys", "entity", ":kind", ":id");
        String surveyRunPath = mkPath("data-extract", "survey-run", ":id");
        String surveyInstanceResponsesPath = mkPath("data-extract", "survey-run-response", "instance", ":id");
        String surveyRunResponsesPath = mkPath("data-extract", "survey-run-response", ":id");

        get(surveyRunPath, (request, response) -> {
            long runId = getId(request);

            LOG.info("Survey run has been exported successfully");
            return writeExtract(
                    mkFilename(getSurveyRunNameById(runId)),
                    getSurveyRunInstances(runId),
                    request,
                    response);
        });

        get(surveyInstanceResponsesPath, (request, response) -> {
            long instanceId = getId(request);

            LOG.info("Survey instance with responses has been exported successfully");
            return writeExtract(
                    mkFilename(getSurveyRunNameByInstanceId(instanceId)),
                    getSurveyInstanceResponses(instanceId),
                    request,
                    response);
        });

        get(surveyRunResponsesPath, (request, response) -> {
            long runId = getId(request);

            LOG.info("Survey run with responses has been exported successfully");
            return writeExtract(
                    mkFilename(getSurveyRunNameById(runId)),
                    getSurveyRunWithResponses(runId),
                    request,
                    response);
        });
        
        get(surveysForEntityPath, (request, response) -> {
            EntityReference ref = getEntityReference(request);
            
            LOG.info("Survey information for entity has been exported successfully");
            return writeExtract(
                    mkFilename(format("%s-%d", ref.kind().name().toLowerCase(), ref.id())),
                    getSurveysForEntity(ref),
                    request,
                    response);
        });
    }


    private SelectWhereStep<?> getSurveysForEntity(EntityReference entityReference) {

        IdSelectionOptions idSelectionOptions = mkOpts(entityReference);

        GenericSelector genericSelectorForChangeInitiativeIds = genericSelectorFactory.applyForKind(EntityKind.CHANGE_INITIATIVE, idSelectionOptions);
        GenericSelector genericSelectorForApplications = genericSelectorFactory.applyForKind(EntityKind.APPLICATION, idSelectionOptions);

        SelectConditionStep<Record14<String, String, String, String, Long, String, String, String, String, Date, String, Date, String, Date>> applicationSurveySelect = DSL
                .select(ORGANISATIONAL_UNIT.NAME.as("Org Unit"),
                        APPLICATION.ASSET_CODE.as("Entity Id"),
                        APPLICATION.NAME.as("Entity Name"),
                        SURVEY_INSTANCE.ENTITY_KIND.as("Entity Kind"),
                        SURVEY_INSTANCE.ID.as("Survey Instance Id"),
                        SURVEY_TEMPLATE.NAME.as("Survey Template"),
                        SURVEY_RUN.NAME.as("Survey Run"),
                        PERSON.DISPLAY_NAME.as("Survey Owner"),
                        SURVEY_INSTANCE.STATUS.as("Status"),
                        SURVEY_INSTANCE.DUE_DATE.as("Due Date"),
                        SURVEY_INSTANCE.SUBMITTED_BY.as("Submitted By"),
                        DSL.date(SURVEY_INSTANCE.SUBMITTED_AT).as("Submitted At"),
                        SURVEY_INSTANCE.APPROVED_BY.as("Approved By"),
                        DSL.date(SURVEY_INSTANCE.APPROVED_AT).as("Approved At"))
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID)
                        .and(SURVEY_TEMPLATE.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .innerJoin(APPLICATION).on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(APPLICATION.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                .innerJoin(ORGANISATIONAL_UNIT).on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .leftJoin(PERSON).on(SURVEY_RUN.OWNER_ID.eq(PERSON.ID))
                .where(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull()
                        .and(APPLICATION.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name())
                                .and(APPLICATION.IS_REMOVED.isFalse()
                                        .and(APPLICATION.ID.in(genericSelectorForApplications.selector())))));


        SelectConditionStep<Record14<String, String, String, String, Long, String, String, String, String, Date, String, Date, String, Date>> changeInitiativeSurveySelect = DSL
                .select(ORGANISATIONAL_UNIT.NAME.as("Org Unit"),
                        CHANGE_INITIATIVE.EXTERNAL_ID.as("Entity Id"),
                        CHANGE_INITIATIVE.NAME.as("Entity Name"),
                        SURVEY_INSTANCE.ENTITY_KIND.as("Entity Kind"),
                        SURVEY_INSTANCE.ID.as("Survey Instance Id"),
                        SURVEY_TEMPLATE.NAME.as("Survey Template"),
                        SURVEY_RUN.NAME.as("Survey Run"),
                        PERSON.DISPLAY_NAME.as("Survey Owner"),
                        SURVEY_INSTANCE.STATUS.as("Status"),
                        SURVEY_INSTANCE.DUE_DATE.as("Due Date"),
                        SURVEY_INSTANCE.SUBMITTED_BY.as("Submitted By"),
                        DSL.date(SURVEY_INSTANCE.SUBMITTED_AT).as("Submitted At"),
                        SURVEY_INSTANCE.APPROVED_BY.as("Approved By"),
                        DSL.date(SURVEY_INSTANCE.APPROVED_AT).as("Approved At"))
                .from(SURVEY_INSTANCE)
                .innerJoin(SURVEY_RUN).on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .innerJoin(SURVEY_TEMPLATE).on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_TEMPLATE.ID)
                        .and(SURVEY_TEMPLATE.TARGET_ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name())))
                .innerJoin(CHANGE_INITIATIVE).on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name())
                        .and(CHANGE_INITIATIVE.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                .innerJoin(ORGANISATIONAL_UNIT).on(CHANGE_INITIATIVE.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .leftJoin(PERSON).on(SURVEY_RUN.OWNER_ID.eq(PERSON.ID))
                .where(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull()
                        .and(CHANGE_INITIATIVE.ID.in(genericSelectorForChangeInitiativeIds.selector())));

        return dsl
                .selectFrom(applicationSurveySelect.union(changeInitiativeSurveySelect)
                        .asTable());
    }


    private SelectConditionStep<?> getSurveyRunInstances(long surveyRunId) {
        return dsl
                .select(APPLICATION.NAME.coalesce(CHANGE_INITIATIVE.NAME).as("Entity Name"),
                        APPLICATION.ASSET_CODE.coalesce(CHANGE_INITIATIVE.EXTERNAL_ID).as("Entity Id"),
                        SURVEY_INSTANCE.ENTITY_KIND.as("Entity Kind"),
                        SURVEY_INSTANCE.STATUS.as("Status"))
                .select(SURVEY_INSTANCE_FIELDS)
                .from(SURVEY_INSTANCE)
                .leftOuterJoin(APPLICATION).on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(APPLICATION.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                .leftOuterJoin(CHANGE_INITIATIVE).on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name())
                        .and(CHANGE_INITIATIVE.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .and(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull());
    }

    private SelectSeekStep2<Record, Integer, String> getSurveyInstanceResponses(
            long surveyInstanceId) {

        return dsl.select(SURVEY_RESPONSE_FIELDS)
                .from(SURVEY_QUESTION)
                .join(SURVEY_QUESTION_RESPONSE)
                .on(SURVEY_QUESTION_RESPONSE.QUESTION_ID.eq(SURVEY_QUESTION.ID))
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .orderBy(SURVEY_QUESTION.POSITION, SURVEY_QUESTION.QUESTION_TEXT);
    }

    private SelectConditionStep<?> getSurveyRunWithResponses(long surveyRunId) {
        return dsl.select(
                APPLICATION.NAME.coalesce(CHANGE_INITIATIVE.NAME).as("Entity Name"),
                APPLICATION.ASSET_CODE.coalesce(CHANGE_INITIATIVE.EXTERNAL_ID).as("Entity Id"),
                SURVEY_INSTANCE.ENTITY_KIND.as("Entity Kind"),
                SURVEY_INSTANCE.STATUS.as("Status"))
                .select(SURVEY_RESPONSE_FIELDS)
                .select(PERSON.EMAIL.as("Participant Email"))
                .select(SURVEY_INSTANCE_FIELDS)
                .from(SURVEY_INSTANCE)
                .leftOuterJoin(APPLICATION).on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                        .and(APPLICATION.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                .leftOuterJoin(CHANGE_INITIATIVE).on(SURVEY_INSTANCE.ENTITY_KIND.eq(EntityKind.CHANGE_INITIATIVE.name())
                        .and(CHANGE_INITIATIVE.ID.eq(SURVEY_INSTANCE.ENTITY_ID)))
                .join(SURVEY_RUN).on(SURVEY_RUN.ID.eq(SURVEY_INSTANCE.SURVEY_RUN_ID))
                .join(SURVEY_QUESTION).on(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.eq(SURVEY_RUN.SURVEY_TEMPLATE_ID))
                .leftOuterJoin(SURVEY_QUESTION_RESPONSE).on(SURVEY_QUESTION_RESPONSE.QUESTION_ID.eq(SURVEY_QUESTION.ID)
                        .and(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(SURVEY_INSTANCE.ID)))
                .join(PERSON).on(PERSON.ID.eq(SURVEY_QUESTION_RESPONSE.PERSON_ID))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .and(SURVEY_INSTANCE.ORIGINAL_INSTANCE_ID.isNull());
    }


    private String mkFilename(String postfix) {
        return "survey-run-instances-" + postfix;
    }


    private String getSurveyRunNameById(long id) {
        return dsl.select(SURVEY_RUN.NAME)
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.ID.eq(id))
                .fetchOne().component1();
    }


    private String getSurveyRunNameByInstanceId(long surveyInstanceId) {
        return dsl.select(SURVEY_RUN.NAME)
                .from(SURVEY_RUN)
                .where(SURVEY_RUN.ID.in(
                        dsl.select(SURVEY_INSTANCE.SURVEY_RUN_ID)
                                .from(SURVEY_INSTANCE)
                                .where(SURVEY_INSTANCE.ID.eq(surveyInstanceId))
                ))
                .fetchOne().component1();
    }

}