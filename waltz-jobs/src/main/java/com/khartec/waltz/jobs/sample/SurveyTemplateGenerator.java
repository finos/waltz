/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.jobs.sample;


import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.ImmutableReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.ReleaseLifecycleStatus;
import com.khartec.waltz.model.ReleaseLifecycleStatusChangeCommand;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.service.survey.SurveyTemplateService;
import org.jooq.DSLContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;
import static com.khartec.waltz.schema.tables.SurveyTemplate.SURVEY_TEMPLATE;

/**
 * Generates random survey templates and associated questions
 */
public class SurveyTemplateGenerator {

    public static void main(String[] args) {

        try {

            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
            DSLContext dsl = ctx.getBean(DSLContext.class);
            SurveyTemplateService surveyTemplateService = ctx.getBean(SurveyTemplateService.class);
            SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);

            dsl.deleteFrom(SURVEY_TEMPLATE).execute();
            dsl.deleteFrom(SURVEY_QUESTION).execute();

            ReleaseLifecycleStatusChangeCommand statusChangeCommand = ImmutableReleaseLifecycleStatusChangeCommand.builder()
                    .newStatus(ReleaseLifecycleStatus.ACTIVE)
                    .build();

            SurveyTemplateChangeCommand appSurvey = mkAppSurvey();
            long aid = surveyTemplateService.create("admin", appSurvey);
            List<SurveyQuestion> appQs = mkAppQuestions(aid);
            appQs.forEach(surveyQuestionService::create);
            surveyTemplateService.updateStatus("admin", aid, statusChangeCommand);

            SurveyTemplateChangeCommand projectSurvey = mkProjectSurvey();
            long pid = surveyTemplateService.create("admin", projectSurvey);
            List<SurveyQuestion> projQs = mkProjQuestions(pid);
            projQs.forEach(surveyQuestionService::create);
            surveyTemplateService.updateStatus("admin", pid, statusChangeCommand);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    private static SurveyTemplateChangeCommand mkAppSurvey() {
        return ImmutableSurveyTemplateChangeCommand.builder()
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


}


