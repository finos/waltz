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

package org.finos.waltz.jobs.generators;


import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.common.RandomUtilities;
import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.app_group.AppGroupDao;
import org.finos.waltz.data.involvement_kind.InvolvementKindDao;
import org.finos.waltz.data.person.PersonDao;
import org.finos.waltz.data.survey.SurveyTemplateDao;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.HierarchyQueryScope;
import org.finos.waltz.model.app_group.AppGroup;
import org.finos.waltz.model.involvement_kind.InvolvementKind;
import org.finos.waltz.model.person.Person;
import org.finos.waltz.model.survey.*;
import org.finos.waltz.schema.tables.records.SurveyQuestionResponseRecord;
import org.finos.waltz.schema.tables.records.SurveyRunRecord;
import org.finos.waltz.service.involvement_group.InvolvementGroupService;
import org.finos.waltz.service.survey.SurveyInstanceService;
import org.finos.waltz.service.survey.SurveyQuestionService;
import org.finos.waltz.service.survey.SurveyRunService;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;
import static org.finos.waltz.common.Checks.checkFalse;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.DateTimeUtilities.*;
import static org.finos.waltz.common.ListUtilities.map;
import static org.finos.waltz.common.RandomUtilities.randomPick;
import static org.finos.waltz.schema.Tables.SURVEY_INSTANCE;
import static org.finos.waltz.schema.Tables.SURVEY_RUN;
import static org.finos.waltz.schema.tables.Person.PERSON;
import static org.finos.waltz.schema.tables.SurveyInstanceRecipient.SURVEY_INSTANCE_RECIPIENT;
import static org.finos.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;
import static org.jooq.lambda.tuple.Tuple.tuple;

/**
 * Generates random survey runs and associated instances and recipients
 */
public class SurveyRunGenerator implements SampleDataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(SurveyRunGenerator.class);


    private static final int NUMBER_OF_RUNS = 20;
    private static final int MAX_INVOLVEMENT_KINDS_PER_RUN = 3;
    private static final int MAX_SURVEY_AGE_IN_DAYS = 30;
    private static final int MAX_SURVEY_LIFESPAN_IN_DAYS = 120;

    private static final String ID_SEPARATOR = ";";

    private static final Random random = RandomUtilities.getRandom();

    private static final String[] SURVEY_RUN_PREFIXES = {"ANNUAL", "Q1", "Q2", "Q3", "Q4"};
    private static final String SURVEY_RUN_SUFFIX = "(GENERATOR)"; // so we can delete previous generated data before rerun

    private static final String[] SURVEY_QUESTION_STRING_RESPONSES = {
            "This is a simple one-liner response.",
            "This is a long response with multiple lines.\n This should be associated with a TEXT_AREA question type. We expect the UI to display this response with line-breaks."
    };

    private static final String[] SURVEY_QUESTION_RESPONSE_COMMENTS = {
            "This is a simple one-liner response comment.",
            "This is a long response comment with multiple lines.\n We expect the UI to display this comment with line-breaks."
    };


    private static SurveyRunRecord mkRandomSurveyRunRecord(DSLContext dsl,
                                                           List<AppGroup> appGroups,
                                                           SurveyTemplate surveyTemplate,
                                                           Person owner) {


        String surveyRunName = String.format("%s %s %s",
                randomPick(SURVEY_RUN_PREFIXES),
                surveyTemplate.name(),
                SURVEY_RUN_SUFFIX);

        SurveyRunRecord surveyRunRecord = dsl.newRecord(SURVEY_RUN);
        surveyRunRecord.setOwnerId(owner.id().get());
        surveyRunRecord.setContactEmail(owner.email());
        surveyRunRecord.setSurveyTemplateId(surveyTemplate.id().get());
        surveyRunRecord.setName(surveyRunName);
        surveyRunRecord.setDescription(surveyTemplate.description());
        surveyRunRecord.setSelectorEntityKind(EntityKind.APP_GROUP.name());
        surveyRunRecord.setSelectorEntityId(appGroups.get(random.nextInt(appGroups.size())).id().get());
        surveyRunRecord.setSelectorHierarchyScope(HierarchyQueryScope.EXACT.name());
        surveyRunRecord.setIssuanceKind(randomPick(SurveyIssuanceKind.values()).name());
        LocalDate issuedOn = LocalDate.now().minusDays(random.nextInt(MAX_SURVEY_AGE_IN_DAYS));
        surveyRunRecord.setIssuedOn(java.sql.Date.valueOf(issuedOn));
        surveyRunRecord.setDueDate(java.sql.Date.valueOf(issuedOn.plusDays(random.nextInt(MAX_SURVEY_LIFESPAN_IN_DAYS))));
        surveyRunRecord.setApprovalDueDate(java.sql.Date.valueOf(issuedOn.plusDays(random.nextInt(MAX_SURVEY_LIFESPAN_IN_DAYS))));
        surveyRunRecord.setStatus(randomPick(SurveyRunStatus.ISSUED.name(), SurveyRunStatus.COMPLETED.name()));

        return surveyRunRecord;
    }


    private static List<SurveyInstanceQuestionResponse> mkRandomSurveyRunResponses(long surveyRunId,
                                                                                   SurveyInstanceService surveyInstanceService,
                                                                                   SurveyQuestionService surveyQuestionService) {

        List<SurveyQuestion> surveyQuestions = surveyQuestionService.findForSurveyRun(surveyRunId);
        Set<SurveyInstance> surveyInstances = surveyInstanceService.findForSurveyRun(surveyRunId);

        Map<Long, List<Tuple2<Long, Person>>> surveyInstanceRecipientsMap = surveyInstances
                .stream()
                .flatMap(surveyInstance -> surveyInstanceService.findRecipients(surveyInstance.id().get())
                        .stream()
                        .map(p -> tuple(surveyInstance.id().get(), p)))
                .collect(groupingBy(t -> t.v1));

        return surveyInstanceRecipientsMap.entrySet()
                .stream()
                .flatMap(entry -> {
                    Long surveyInstanceId = entry.getKey();
                    List<Person> instanceRecipients = map(entry.getValue(), t -> t.v2);

                    return surveyQuestions.stream()
                            .map(q -> ImmutableSurveyInstanceQuestionResponse.builder()
                                    .surveyInstanceId(surveyInstanceId)
                                    .personId(randomPick(instanceRecipients).id().get())
                                    .questionResponse(mkRandomSurveyQuestionResponse(q, random))
                                    .lastUpdatedAt(nowUtc())
                                    .build());
                })
                .collect(toList());

    }


    private static SurveyQuestionResponse mkRandomSurveyQuestionResponse(SurveyQuestion surveyQuestion, Random random) {
        SurveyQuestionFieldType fieldType = surveyQuestion.fieldType();
        return ImmutableSurveyQuestionResponse.builder()
                .questionId(surveyQuestion.id().get())
                .booleanResponse((fieldType == SurveyQuestionFieldType.BOOLEAN)
                        ? Optional.of(random.nextBoolean())
                        : Optional.empty())
                .numberResponse((fieldType == SurveyQuestionFieldType.NUMBER)
                        ? Optional.of(Double.valueOf(random.nextInt()))
                        : Optional.empty())
                .stringResponse((fieldType == SurveyQuestionFieldType.TEXT || fieldType == SurveyQuestionFieldType.TEXTAREA)
                        ? Optional.of(randomPick(SURVEY_QUESTION_STRING_RESPONSES))
                        : Optional.empty())
                .comment(random.nextBoolean()
                        ? Optional.of(randomPick(SURVEY_QUESTION_RESPONSE_COMMENTS))
                        : Optional.empty())
                .build();
    }


    @Override
    public Map<String, Integer> create(ApplicationContext ctx) {
        final DSLContext dsl = ctx.getBean(DSLContext.class);

        List<Person> owners = dsl
                .selectFrom(PERSON)
                .limit(NUMBER_OF_RUNS)
                .fetch()
                .map(PersonDao.personMapper);
        checkFalse(isEmpty(owners), "No person found, please generate person data first");

        final SurveyTemplateDao surveyTemplateDao = ctx.getBean(SurveyTemplateDao.class);
        List<SurveyTemplate> surveyTemplates = surveyTemplateDao.findAllActive();
        checkFalse(isEmpty(surveyTemplates), "No template found, please generate templates first");

        final AppGroupDao appGroupDao = ctx.getBean(AppGroupDao.class);
        List<AppGroup> appGroups = appGroupDao.findPublicGroups();
        checkFalse(isEmpty(appGroups), "No public app group found, please generate app groups first");


        final InvolvementKindDao involvementKindDao = ctx.getBean(InvolvementKindDao.class);
        List<InvolvementKind> involvementKinds = involvementKindDao.findAll();

        final SurveyRunService surveyRunService = ctx.getBean(SurveyRunService.class);
        final SurveyInstanceService surveyInstanceService = ctx.getBean(SurveyInstanceService.class);
        final SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);

        AtomicInteger surveyCompletedCount = new AtomicInteger(0);
        IntStream.range(0, NUMBER_OF_RUNS).forEach(idx -> {
            SurveyTemplate surveyTemplate = surveyTemplates.get(random.nextInt(surveyTemplates.size()));
            Person owner = owners.get(random.nextInt(owners.size()));

            SurveyRunRecord surveyRunRecord = mkRandomSurveyRunRecord(dsl, appGroups, surveyTemplate, owner);
            surveyRunRecord.store();
            long surveyRunId = surveyRunRecord.getId();
            LOG.debug("Survey Run: {} / {} / {}", surveyRunRecord.getStatus(), surveyRunId, surveyRunRecord.getName());

            surveyRunService
                    .createRecipientsGroup(
                            surveyRunId,
                            surveyRunRecord.getName(),
                            SetUtilities.map(involvementKinds, d -> d.id().get()),
                            "surveyRunGenerator");

            ImmutableInstancesAndRecipientsCreateCommand createCmd = ImmutableInstancesAndRecipientsCreateCommand.builder()
                    .surveyRunId(surveyRunId)
                    .dueDate(toLocalDate(nowUtcTimestamp()))
                    .approvalDueDate(toLocalDate(nowUtcTimestamp()))
                    .excludedRecipients(emptySet())
                    .build();
            surveyRunService.createSurveyInstancesAndRecipients(createCmd);

            List<SurveyInstanceQuestionResponse> surveyInstanceQuestionResponses = mkRandomSurveyRunResponses(
                    surveyRunId, surveyInstanceService, surveyQuestionService);

            Map<SurveyInstanceStatus, Set<Long>> surveyInstanceStatusMap = surveyInstanceQuestionResponses.stream()
                    .mapToLong(response -> response.surveyInstanceId())
                    .distinct()
                    .mapToObj(id -> tuple(randomPick(
                            SurveyInstanceStatus.NOT_STARTED, SurveyInstanceStatus.IN_PROGRESS, SurveyInstanceStatus.COMPLETED), id))
                    .collect(groupingBy(t -> t.v1, mapping(t -> t.v2, toSet())));

            dsl.batchInsert(surveyInstanceQuestionResponses.stream()
                    .map(r -> {
                        if (surveyInstanceStatusMap.containsKey(SurveyInstanceStatus.NOT_STARTED)
                                && surveyInstanceStatusMap.get(SurveyInstanceStatus.NOT_STARTED).contains(r.surveyInstanceId())) {
                            return null;    // don't create response for NOT_STARTED
                        }

                        SurveyQuestionResponse questionResponse = r.questionResponse();
                        SurveyQuestionResponseRecord record = new SurveyQuestionResponseRecord();
                        record.setSurveyInstanceId(r.surveyInstanceId());
                        record.setPersonId(r.personId());
                        record.setQuestionId(questionResponse.questionId());
                        record.setBooleanResponse(questionResponse.booleanResponse().orElse(null));
                        record.setNumberResponse(questionResponse.numberResponse().map(BigDecimal::valueOf).orElse(null));
                        record.setStringResponse(questionResponse.stringResponse().orElse(null));
                        record.setComment(r.questionResponse().comment().orElse(null));
                        record.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());

                        return record;
                    })
                    .filter(Objects::nonNull)
                    .collect(toList()))
                    .execute();

            if (SurveyRunStatus.valueOf(surveyRunRecord.getStatus()) == SurveyRunStatus.COMPLETED) {
                surveyRunRecord.setStatus(SurveyRunStatus.COMPLETED.name());
                surveyRunRecord.store();

                surveyCompletedCount.incrementAndGet();

                // update instances to COMPLETED
                if (surveyInstanceStatusMap.containsKey(SurveyInstanceStatus.COMPLETED)) {
                    Set<Long> completedInstanceIds = surveyInstanceStatusMap.get(SurveyInstanceStatus.COMPLETED);
                    dsl.update(SURVEY_INSTANCE)
                            .set(SURVEY_INSTANCE.STATUS, SurveyInstanceStatus.COMPLETED.name())
                            .where(SURVEY_INSTANCE.ID.in(completedInstanceIds))
                            .execute();
                    LOG.debug(" --- {} instances: {}", SurveyInstanceStatus.COMPLETED, completedInstanceIds);
                }

            } else {
                surveyInstanceStatusMap.forEach(((status, instanceIds) -> {
                    dsl.update(SURVEY_INSTANCE)
                            .set(SURVEY_INSTANCE.STATUS, status.name())
                            .where(SURVEY_INSTANCE.ID.in(instanceIds))
                            .execute();
                    LOG.debug(" --- {} instances: {}", status.name(), instanceIds);
                }));
            }
        });

        LOG.debug("Generated: {} survey runs, in which {} are completed", NUMBER_OF_RUNS, surveyCompletedCount.get());

        return null;
    }

    @Override
    public boolean remove(ApplicationContext ctx) {
        DSLContext dsl = getDsl(ctx);

        Condition previousSurveyRunCondition = SURVEY_RUN.NAME.like("% " + SURVEY_RUN_SUFFIX);

        Select<Record1<Long>> surveyRunIdSelector = DSL
                .select(SURVEY_RUN.ID)
                .from(SURVEY_RUN)
                .where(previousSurveyRunCondition);

        Select<Record1<Long>> surveyInstanceIdSelector = DSL
                .select(SURVEY_INSTANCE.ID)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.in(surveyRunIdSelector));

        int deleteCount = dsl
                .deleteFrom(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.in(surveyInstanceIdSelector))
                .execute();
        LOG.debug("Deleted: {} existing question responses", deleteCount);

        deleteCount = dsl
                .deleteFrom(SURVEY_INSTANCE_RECIPIENT)
                .where(SURVEY_INSTANCE_RECIPIENT.SURVEY_INSTANCE_ID.in(surveyInstanceIdSelector))
                .execute();
        LOG.debug("Deleted: {} existing instance recipients", deleteCount);

        deleteCount = dsl
                .deleteFrom(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.in(surveyRunIdSelector))
                .execute();
        LOG.debug("Deleted: {} existing survey instances", deleteCount);

        deleteCount = dsl
                .deleteFrom(SURVEY_RUN)
                .where(previousSurveyRunCondition)
                .execute();
        LOG.debug("Deleted: {} existing survey runs", deleteCount);
        return false;
    }
}
