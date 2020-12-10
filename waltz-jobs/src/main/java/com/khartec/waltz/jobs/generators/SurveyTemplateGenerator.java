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

package com.khartec.waltz.jobs.generators;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.ReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.service.survey.SurveyTemplateService;
import org.jooq.DSLContext;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.Tables.SURVEY_QUESTION_RESPONSE;
import static com.khartec.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;
import static com.khartec.waltz.schema.tables.SurveyTemplate.SURVEY_TEMPLATE;

/**
 * Generates random survey templates and associated questions
 */
public class SurveyTemplateGenerator implements SampleDataGenerator {

    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {

        SurveyTemplateService surveyTemplateService = ctx.getBean(SurveyTemplateService.class);
        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);

        SurveyTemplateChangeCommand appSurvey = mkAppSurvey();
        long aid = surveyTemplateService.create("admin", appSurvey);
        List<SurveyQuestion> appQs = mkAppQuestions(aid);
        appQs.forEach(surveyQuestionService::create);


        SurveyTemplateChangeCommand projectSurvey = mkProjectSurvey();
        long pid = surveyTemplateService.create("admin", projectSurvey);
        List<SurveyQuestion> projQs = mkProjQuestions(pid);
        projQs.forEach(surveyQuestionService::create);

        surveyTemplateService.updateStatus("admin", aid, ImmutableReleaseLifecycleStatusChangeCommand.builder().newStatus(ReleaseLifecycleStatus.ACTIVE).build());
        surveyTemplateService.updateStatus("admin", pid, ImmutableReleaseLifecycleStatusChangeCommand.builder().newStatus(ReleaseLifecycleStatus.ACTIVE).build());

        return null;
    }


    private static SurveyTemplateChangeCommand mkAppSurvey() {
        return ImmutableSurveyTemplateChangeCommand
                .builder()
                .name("App Survey")
                .description("Questions about your application")
                .targetEntityKind(EntityKind.APPLICATION)
                .build();
    }


    private static List<SurveyQuestion> mkAppQuestions(long templateId) {
        return newArrayList(
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("Is your app accessible via a browser")
                        .helpText("IE11, Chrome, FFox etc")
                        .isMandatory(true)
                        .fieldType(SurveyQuestionFieldType.BOOLEAN)
                        .surveyTemplateId(templateId)
                        .position(10)
                        .build(),
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("What percentage of your code base has tests")
                        .helpText("Approximation is fine (0-100)")
                        .isMandatory(true)
                        .allowComment(true)
                        .surveyTemplateId(templateId)
                        .fieldType(SurveyQuestionFieldType.NUMBER)
                        .position(20)
                        .build(),
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("What is the primary goal for the next release")
                        .isMandatory(true)
                        .surveyTemplateId(templateId)
                        .fieldType(SurveyQuestionFieldType.TEXTAREA)
                        .position(30)
                        .build(),
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("Who is your primary customer")
                        .surveyTemplateId(templateId)
                        .fieldType(SurveyQuestionFieldType.TEXT)
                        .position(40)
                        .build()
        );
    }


    private static SurveyTemplateChangeCommand mkProjectSurvey() {
        return ImmutableSurveyTemplateChangeCommand.builder()
                .name("Programme Survey")
                .description("Questions about your programme governance")
                .targetEntityKind(EntityKind.CHANGE_INITIATIVE)
                .build();
    }


    private static List<SurveyQuestion> mkProjQuestions(long templateId) {

        return newArrayList(
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("Does this program change operational risk ?")
                        .helpText("If yes add a comment")
                        .isMandatory(true)
                        .surveyTemplateId(templateId)
                        .allowComment(true)
                        .fieldType(SurveyQuestionFieldType.BOOLEAN)
                        .position(10)
                        .build(),
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("How many months will this programme take to implement?")
                        .allowComment(true)
                        .isMandatory(true)
                        .surveyTemplateId(templateId)
                        .fieldType(SurveyQuestionFieldType.NUMBER)
                        .position(20)
                        .build(),
                ImmutableSurveyQuestion
                        .builder()
                        .questionText("What steps have you taken to ensure compliance with policies?")
                        .isMandatory(true)
                        .surveyTemplateId(templateId)
                        .fieldType(SurveyQuestionFieldType.TEXTAREA)
                        .position(30)
                        .build(),
                ImmutableSurveyQuestion
                        .builder()
                        .surveyTemplateId(templateId)
                        .questionText("Who is the primary stakeholder?")
                        .fieldType(SurveyQuestionFieldType.TEXT)
                        .position(40)
                        .build()
        );
    }



    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);
        dsl.deleteFrom(SURVEY_QUESTION_RESPONSE).execute();
        dsl.deleteFrom(SURVEY_QUESTION).execute();
        dsl.deleteFrom(SURVEY_TEMPLATE).execute();
        return true;
    }
}


