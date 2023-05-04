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

package org.finos.waltz.jobs.harness;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.common.exception.InsufficientPrivelegeException;
import org.finos.waltz.model.*;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.service.DIConfiguration;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.service.survey.SurveyRunService;
import org.finos.waltz.service.survey.SurveyTemplateService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.stream.LongStream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.DateTimeUtilities.nowUtcTimestamp;
import static org.finos.waltz.common.DateTimeUtilities.toLocalDate;


public class SurveyHarness {

    public static void main(String[] args) throws InsufficientPrivelegeException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        surveyRunHarness(ctx);
//        surveyTempateHarness(ctx);
    }

    private static void surveyTempateHarness(AnnotationConfigApplicationContext ctx) {
        SurveyTemplateService surveyTemplateService = ctx.getBean(SurveyTemplateService.class);
        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);

        SurveyTemplateChangeCommand surveyTemplateChangeCommand = ImmutableSurveyTemplateChangeCommand.builder()
                .name("AAA")
                .description("BBB")
                .targetEntityKind(EntityKind.CHANGE_INITIATIVE)
                .build();

        long templateId = surveyTemplateService.create("admin", surveyTemplateChangeCommand);
        System.out.println("Created: template create with ID = " + templateId);

        SurveyQuestion surveyQuestion = ImmutableSurveyQuestion.builder()
                .surveyTemplateId(templateId)
                .sectionName("SSS")
                .questionText("QQQ")
                .helpText("HHH")
                .fieldType(SurveyQuestionFieldType.TEXTAREA)
                .position(1)
                .isMandatory(false)
                .allowComment(true)
                .build();

        long questionId = surveyQuestionService.create(surveyQuestion);
        System.out.println("Created: question create with ID = " + questionId);
    }


    private static void surveyRunHarness(AnnotationConfigApplicationContext ctx) throws InsufficientPrivelegeException {
        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);
        surveyQuestionService.findForSurveyTemplate(1).forEach(System.out::println);

        IdSelectionOptions idSelectionOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference.mkRef(EntityKind.APP_GROUP, 1))
                .scope(HierarchyQueryScope.EXACT)
                .build();

        SurveyRunCreateCommand surveyRunCreateCommand = ImmutableSurveyRunCreateCommand.builder()
                .surveyTemplateId(1L)
                .name("Q1 Quality Survey")
                .selectionOptions(idSelectionOptions)
                .issuanceKind(SurveyIssuanceKind.INDIVIDUAL)
                .involvementKindIds(SetUtilities.fromCollection(
                        LongStream.range(1, 5).mapToObj(Long::valueOf)
                        .collect(toList())))
                .contactEmail("jack.livingston12@gmail.com")
                .build();

        SurveyRunService surveyRunService = ctx.getBean(SurveyRunService.class);

        String userName = "livingston@mail.com";
        long surveyRunId = surveyRunService.createSurveyRun(userName, surveyRunCreateCommand).id().get();

        ImmutableInstancesAndRecipientsCreateCommand createCmd = ImmutableInstancesAndRecipientsCreateCommand.builder()
                .surveyRunId(surveyRunId)
                .dueDate(toLocalDate(nowUtcTimestamp()))
                .approvalDueDate(toLocalDate(nowUtcTimestamp()))
                .excludedRecipients(emptySet())
                .build();

        List<SurveyInstanceRecipient> surveyInstanceRecipients = surveyRunService.generateSurveyInstanceRecipients(createCmd);

        surveyInstanceRecipients.forEach(r -> System.out.println(
                r.surveyInstance().surveyEntity().name().get()
                + " => "
                + r.person().email()));

        System.out.println("Generated recipients count: " + surveyInstanceRecipients.size());

        surveyRunService.createSurveyInstancesAndRecipients(createCmd);

    }




}
