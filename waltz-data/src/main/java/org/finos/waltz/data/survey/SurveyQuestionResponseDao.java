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

package org.finos.waltz.data.survey;


import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.data.InlineSelectFieldFactory;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.survey.CopySurveyResponsesCommand;
import org.finos.waltz.model.survey.ImmutableSurveyInstanceQuestionResponse;
import org.finos.waltz.model.survey.ImmutableSurveyQuestionResponse;
import org.finos.waltz.model.survey.SurveyInstanceQuestionResponse;
import org.finos.waltz.model.survey.SurveyInstanceStatus;
import org.finos.waltz.model.survey.SurveyQuestionResponse;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.records.SurveyQuestionListResponseRecord;
import org.finos.waltz.schema.tables.records.SurveyQuestionResponseRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record12;
import org.jooq.Record6;
import org.jooq.RecordMapper;
import org.jooq.Select;
import org.jooq.SelectConditionStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.lambda.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.finos.waltz.common.Checks.checkNotNull;
import static org.finos.waltz.common.Checks.checkTrue;
import static org.finos.waltz.common.CollectionUtilities.first;
import static org.finos.waltz.common.ListUtilities.isEmpty;
import static org.finos.waltz.common.ListUtilities.newArrayList;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.StringUtilities.ifEmpty;
import static org.finos.waltz.common.StringUtilities.join;
import static org.finos.waltz.model.EntityReference.mkRef;
import static org.finos.waltz.schema.Tables.SURVEY_QUESTION_LIST_RESPONSE;
import static org.finos.waltz.schema.Tables.SURVEY_RUN;
import static org.finos.waltz.schema.tables.SurveyInstance.SURVEY_INSTANCE;
import static org.finos.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;
import static org.jooq.lambda.tuple.Tuple.tuple;

@Repository
public class SurveyQuestionResponseDao {

    private static final org.finos.waltz.schema.tables.SurveyQuestionResponse sqr = SURVEY_QUESTION_RESPONSE.as("sqr");
    private static final org.finos.waltz.schema.tables.SurveyQuestionListResponse sqlr = SURVEY_QUESTION_LIST_RESPONSE.as("sqlr");
    private static final org.finos.waltz.schema.tables.SurveyRun sr = SURVEY_RUN.as("sr");
    private static final org.finos.waltz.schema.tables.SurveyInstance si = SURVEY_INSTANCE.as("si");

    private static final Field<String> entityNameField = InlineSelectFieldFactory.mkNameField(
            SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_ID,
            SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.PERSON, EntityKind.LEGAL_ENTITY));

    private static final Field<String> EXTERNAL_ID_FIELD = InlineSelectFieldFactory.mkExternalIdField(
                    SURVEY_QUESTION_LIST_RESPONSE.ENTITY_ID,
                    SURVEY_QUESTION_LIST_RESPONSE.ENTITY_KIND);

    private static final RecordMapper<Record, SurveyInstanceQuestionResponse> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionResponseRecord record = r.into(SURVEY_QUESTION_RESPONSE);

        Optional<EntityReference> entityReference = Optional.empty();
        if(record.getEntityResponseId() != null && record.getEntityResponseKind() != null) {
            entityReference = Optional.of(mkRef(
                    EntityKind.valueOf(record.getEntityResponseKind()),
                    record.getEntityResponseId(),
                    r.getValue(entityNameField)));
        }

        return ImmutableSurveyInstanceQuestionResponse.builder()
                .surveyInstanceId(record.getSurveyInstanceId())
                .personId(record.getPersonId())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .questionResponse(ImmutableSurveyQuestionResponse.builder()
                        .questionId(record.getQuestionId())
                        .comment(ofNullable(record.getComment()))
                        .stringResponse(ofNullable(record.getStringResponse()))
                        .numberResponse(ofNullable(record.getNumberResponse()).map(BigDecimal::doubleValue))
                        .booleanResponse(ofNullable(record.getBooleanResponse()))
                        .dateResponse(ofNullable(record.getDateResponse()).map(Date::toLocalDate))
                        .entityResponse(entityReference)
                        .build())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyQuestionResponseDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<SurveyInstanceQuestionResponse> findForInstance(long surveyInstanceId) {
        // fetch list responses
        List<Tuple2<SurveyQuestionListResponseRecord, String>> listResponses = dsl
                .select(SURVEY_QUESTION_LIST_RESPONSE.fields())
                .select(EXTERNAL_ID_FIELD)
                .from(SURVEY_QUESTION_LIST_RESPONSE)
                .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .orderBy(SURVEY_QUESTION_LIST_RESPONSE.POSITION)
                .fetch()
                .map(r -> tuple(r.into(SURVEY_QUESTION_LIST_RESPONSE), r.getValue(EXTERNAL_ID_FIELD)));

        Map<Long, List<EntityReference>> entityListResponsesByQuestionId = listResponses
                .stream()
                .filter(d -> d.v1.getEntityKind() != null)
                .map(d -> tuple(
                        d.v1.getQuestionId(),
                        mkRef(EntityKind.valueOf(d.v1.getEntityKind()),
                              d.v1.getEntityId(),
                              d.v1.getResponse(),
                              null,
                              d.v2)))
                .collect(groupingBy(d -> d.v1, mapping(t -> t.v2, toList())));

        Map<Long, List<String>> stringListResponsesByQuestionId = listResponses
                .stream()
                .filter(d -> d.v1.getEntityKind() == null)
                .map(d -> tuple(d.v1.getQuestionId(),d.v1.getResponse()))
                .collect(groupingBy(
                        d -> d.v1,
                        mapping(t -> t.v2, toList())));

        // fetch responses
        List<SurveyInstanceQuestionResponse> responses = dsl
                .select(SURVEY_QUESTION_RESPONSE.fields())
                .select(entityNameField)
                .from(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);

        return responses
                .stream()
                .map(r -> ImmutableSurveyInstanceQuestionResponse
                        .copyOf(r)
                        .withQuestionResponse(ImmutableSurveyQuestionResponse.copyOf(r.questionResponse())
                                .withListResponse(ofNullable(stringListResponsesByQuestionId.get(r.questionResponse().questionId())))
                                .withEntityListResponse(ofNullable(entityListResponsesByQuestionId.get(r.questionResponse().questionId())))))
                .collect(toList());
    }


    public int deletePreviousResponse(Optional<DSLContext> tx, List<SurveyInstanceQuestionResponse> previousResponses) {
        checkNotNull(previousResponses, "responses cannot be null");

        DSLContext dslContext = tx.orElse(dsl);

        if (!previousResponses.isEmpty()) {
            Set<Long> instanceIds = map(
                    previousResponses,
                    SurveyInstanceQuestionResponse::surveyInstanceId);

            checkTrue(instanceIds.size() == 1, "All responses must for the same surveyInstance");
            final Long instanceId = first(previousResponses).surveyInstanceId();

            final Set<Long> previousResponseIds = map(
                    previousResponses,
                    qr -> qr.questionResponse().questionId());

            int rmSingleCount = dslContext
                    .deleteFrom(Tables.SURVEY_QUESTION_RESPONSE)
                    .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(instanceId))
                    .and(SURVEY_QUESTION_RESPONSE.QUESTION_ID.in(previousResponseIds))
                    .execute();

            int rmListCount = dslContext
                    .deleteFrom(SURVEY_QUESTION_LIST_RESPONSE)
                    .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(instanceId))
                    .and(SURVEY_QUESTION_LIST_RESPONSE.QUESTION_ID.in(previousResponseIds))
                    .execute();

            return rmSingleCount + rmListCount;
        } else {
            return 0;
        }
    }


    public void saveResponse(SurveyInstanceQuestionResponse response) {
        checkNotNull(response, "response cannot be null");
        checkNotNull(response.questionResponse(), "response.questionResponse() cannot be null");

        SurveyQuestionResponseRecord record = mkRecord(response);

        Condition responseExistsCondition = DSL
                .exists(DSL
                        .select(SURVEY_QUESTION_RESPONSE.fields())
                        .from(SURVEY_QUESTION_RESPONSE)
                        .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(response.surveyInstanceId())
                            .and(SURVEY_QUESTION_RESPONSE.QUESTION_ID.eq(response.questionResponse().questionId()))));

        // save survey_question_response record
        Boolean responseExists = dsl
                .select(DSL
                        .when(responseExistsCondition, true)
                        .otherwise(false))
                .fetchOne(Record1::value1);

        dsl.transaction(configuration -> {
            DSLContext txDsl = DSL.using(configuration);

            if (responseExists) {
                txDsl.executeUpdate(record);
            } else {
                txDsl.executeInsert(record);
            }

            response.questionResponse()
                    .listResponse()
                    .ifPresent(list -> saveListResponse(
                            txDsl,
                            response.surveyInstanceId(),
                            response.questionResponse().questionId(),
                            list));

            response.questionResponse()
                    .entityListResponse()
                    .ifPresent(list -> saveEntityListResponse(
                            txDsl,
                            response.surveyInstanceId(),
                            response.questionResponse().questionId(),
                            list));
        });
    }



    private <T> void saveGenericListResponse(DSLContext txDsl,
                                             Long instanceId,
                                             Long questionId,
                                             List<T> list,
                                             BiConsumer<T, SurveyQuestionListResponseRecord> recordUpdater) {
        txDsl.deleteFrom(SURVEY_QUESTION_LIST_RESPONSE)
                .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(instanceId))
                .and(SURVEY_QUESTION_LIST_RESPONSE.QUESTION_ID.eq(questionId))
                .execute();

        if (! list.isEmpty()) {
            AtomicInteger counter = new AtomicInteger(0);
            List<SurveyQuestionListResponseRecord> listResponses = list
                    .stream()
                    .map(lr -> {
                        SurveyQuestionListResponseRecord rec = new SurveyQuestionListResponseRecord();
                        rec.setSurveyInstanceId(instanceId);
                        rec.setQuestionId(questionId);
                        rec.setPosition(counter.incrementAndGet());

                        recordUpdater.accept(lr, rec);

                        return rec;
                    })
                    .collect(toList());

            txDsl.batchInsert(listResponses)
                    .execute();
        }
    }


    private void saveListResponse(DSLContext txDsl,
                                  Long instanceId,
                                  Long questionId,
                                  List<String> list) {
        saveGenericListResponse(
                txDsl,
                instanceId,
                questionId,
                list,
                (listItem, record) -> record.setResponse(listItem));
    }


    private void saveEntityListResponse(DSLContext txDsl,
                                  Long instanceId,
                                  Long questionId,
                                  List<EntityReference> list) {
        saveGenericListResponse(
                txDsl,
                instanceId,
                questionId,
                list,
                (ref, record) -> {
                    record.setResponse(ref.name().orElse("?"));
                    record.setEntityId(ref.id());
                    record.setEntityKind(ref.kind().name());
                });
    }


    public void cloneResponses(Optional<DSLContext> tx, long sourceSurveyInstanceId, long targetSurveyInstanceId) {

        DSLContext dslContext = tx.orElse(dsl);

        List<SurveyQuestionResponseRecord> responseRecords = dslContext
                .select(SURVEY_QUESTION_RESPONSE.fields())
                .select(entityNameField)
                .from(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(sourceSurveyInstanceId))
                .fetchInto(SURVEY_QUESTION_RESPONSE)
                .stream()
                .map(r -> {
                    r.setSurveyInstanceId(targetSurveyInstanceId);
                    r.changed(true);
                    return r;
                })
                .collect(toList());

        List<SurveyQuestionListResponseRecord> listResponseRecords = dslContext
                .select(SURVEY_QUESTION_LIST_RESPONSE.fields())
                .from(SURVEY_QUESTION_LIST_RESPONSE)
                .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(sourceSurveyInstanceId))
                .fetchInto(SURVEY_QUESTION_LIST_RESPONSE)
                .stream()
                .map(r -> {
                    r.setSurveyInstanceId(targetSurveyInstanceId);
                    r.changed(true);
                    return r;
                })
                .collect(toList());

        dslContext.transaction(configuration -> {
            DSLContext txDsl = DSL.using(configuration);

            txDsl.batchInsert(responseRecords)
                    .execute();

            txDsl.batchInsert(listResponseRecords)
                    .execute();
        });
    }


    public int deleteForSurveyRun(long surveyRunId) {
        Select<Record1<Long>> surveyInstanceIdSelector = dsl
                .select(Tables.SURVEY_INSTANCE.ID)
                .from(Tables.SURVEY_INSTANCE)
                .where(Tables.SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId));

        // this will also auto delete any survey_question_list_response records (fk delete cascade)
        return dsl
                .delete(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.in(surveyInstanceIdSelector))
                .execute();
    }


    private SurveyQuestionResponseRecord mkRecord(SurveyInstanceQuestionResponse response) {
        SurveyQuestionResponse questionResponse = response.questionResponse();
        Optional<EntityReference> entityResponse = questionResponse.entityResponse();

        SurveyQuestionResponseRecord record = dsl.newRecord(SURVEY_QUESTION_RESPONSE);
        record.setSurveyInstanceId(response.surveyInstanceId());
        record.setQuestionId(questionResponse.questionId());
        record.setPersonId(response.personId());
        record.setLastUpdatedAt(Timestamp.valueOf(response.lastUpdatedAt()));
        record.setComment(questionResponse.comment()
                            .map(c -> ifEmpty(c, null))
                            .orElse(null));
        record.setStringResponse(questionResponse.stringResponse()
                                    .map(s -> ifEmpty(s, null))
                                    .orElse(null));
        record.setNumberResponse(questionResponse.numberResponse()
                                    .map(BigDecimal::valueOf)
                                    .orElse(null));
        record.setBooleanResponse(questionResponse.booleanResponse().orElse(null));
        record.setDateResponse(questionResponse.dateResponse()
                                    .map(DateTimeUtilities::toSqlDate)
                                    .orElse(null));
        record.setEntityResponseKind(entityResponse.map(er -> er.kind().name()).orElse(null));
        record.setEntityResponseId(entityResponse.map(EntityReference::id).orElse(null));
        record.setListResponseConcat(questionResponse.listResponse()
                                        .filter(l -> ! l.isEmpty())
                                        .map(l -> join(l, "; "))
                                        .orElse(null));

        return record;
    }

    public int copyResponses(Long sourceSurveyInstanceId,
                                 CopySurveyResponsesCommand copyCommand,
                                 Long personId) {

        return dsl.transactionResult(configuration -> {
            DSLContext tx = DSL.using(configuration);

            SelectConditionStep<Record1<Long>> sourceTemplateId = tx
                    .select(sr.SURVEY_TEMPLATE_ID)
                    .from(si)
                    .innerJoin(sr).on(si.SURVEY_RUN_ID.eq(sr.ID))
                    .where(si.ID.eq(sourceSurveyInstanceId));

            Table<Record1<Long>> targetInstanceIds = tx
                    .select(si.ID)
                    .from(si)
                    .innerJoin(sr).on(si.SURVEY_RUN_ID.eq(sr.ID)
                            .and(sr.SURVEY_TEMPLATE_ID.eq(sourceTemplateId)))
                    .where(si.ID.in(copyCommand.targetSurveyInstanceIds()))
                    .and(si.STATUS.in(SurveyInstanceStatus.NOT_STARTED.name(), SurveyInstanceStatus.IN_PROGRESS.name()))
                    .asTable();

            int questionResponsesCopiedCount = copySurveyQuestionResponses(
                    tx,
                    sourceSurveyInstanceId,
                    targetInstanceIds,
                    copyCommand,
                    personId);

            int questionListResponsesCopiedCount = copySurveyQuestionListResponses(
                    tx,
                    sourceSurveyInstanceId,
                    targetInstanceIds,
                    copyCommand,
                    personId);

            return questionResponsesCopiedCount;
        });

    }


    private int copySurveyQuestionResponses(DSLContext tx,
                                            Long sourceSurveyInstanceId,
                                            Table<Record1<Long>> targetInstanceIds,
                                            CopySurveyResponsesCommand copyCommand,
                                            Long personId) {

        Condition questionCondition = isEmpty(copyCommand.questionIds())
                ? DSL.trueCondition()
                : sqr.QUESTION_ID.in(copyCommand.questionIds());


        if(copyCommand.overrideExistingResponses()){
            tx
                    .deleteFrom(sqr)
                    .where(sqr.SURVEY_INSTANCE_ID.in(copyCommand.targetSurveyInstanceIds())
                            .and(questionCondition))
                    .execute();
        }

        org.finos.waltz.schema.tables.SurveyQuestionResponse existing_target_sqr = SURVEY_QUESTION_RESPONSE.as("existing_target_sqr");

        SelectConditionStep<Record12<Long, Long, String, Boolean, BigDecimal, Date, Long, String, String, Timestamp, Long, String>> responsesToInsert = tx
                .select(
                        targetInstanceIds.field(si.ID).as("siId"),
                        sqr.QUESTION_ID,
                        sqr.STRING_RESPONSE,
                        sqr.BOOLEAN_RESPONSE,
                        sqr.NUMBER_RESPONSE,
                        sqr.DATE_RESPONSE,
                        sqr.ENTITY_RESPONSE_ID,
                        sqr.ENTITY_RESPONSE_KIND,
                        sqr.LIST_RESPONSE_CONCAT,
                        DSL.val(DateTimeUtilities.nowUtcTimestamp()),
                        DSL.val(personId).as("person_id"),
                        sqr.COMMENT)
                .from(sqr)
                .crossJoin(targetInstanceIds)
                .leftJoin(existing_target_sqr)
                .on(sqr.QUESTION_ID.eq(existing_target_sqr.QUESTION_ID))
                .and(targetInstanceIds.field(si.ID).eq(existing_target_sqr.SURVEY_INSTANCE_ID))
                .where(sqr.SURVEY_INSTANCE_ID.eq(sourceSurveyInstanceId))
                .and(questionCondition)
                .and(existing_target_sqr.SURVEY_INSTANCE_ID.isNull());

        return tx
                .insertInto(SURVEY_QUESTION_RESPONSE)
                .columns(
                        SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID,
                        SURVEY_QUESTION_RESPONSE.QUESTION_ID,
                        SURVEY_QUESTION_RESPONSE.STRING_RESPONSE,
                        SURVEY_QUESTION_RESPONSE.BOOLEAN_RESPONSE,
                        SURVEY_QUESTION_RESPONSE.NUMBER_RESPONSE,
                        SURVEY_QUESTION_RESPONSE.DATE_RESPONSE,
                        SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_ID,
                        SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_KIND,
                        SURVEY_QUESTION_RESPONSE.LIST_RESPONSE_CONCAT,
                        SURVEY_QUESTION_RESPONSE.LAST_UPDATED_AT,
                        SURVEY_QUESTION_RESPONSE.PERSON_ID,
                        SURVEY_QUESTION_RESPONSE.COMMENT)
                .select(responsesToInsert)
                .execute();
    }


    private int copySurveyQuestionListResponses(DSLContext tx,
                                                Long sourceSurveyInstanceId,
                                                Table<Record1<Long>> targetInstanceIds,
                                                CopySurveyResponsesCommand copyCommand,
                                                Long personId) {

        Condition questionCondition = isEmpty(copyCommand.questionIds())
                ? DSL.trueCondition()
                : sqlr.QUESTION_ID.in(copyCommand.questionIds());


        if(copyCommand.overrideExistingResponses()){
            tx
                    .deleteFrom(sqlr)
                    .where(sqlr.SURVEY_INSTANCE_ID.in(copyCommand.targetSurveyInstanceIds())
                            .and(questionCondition))
                    .execute();
        }

        org.finos.waltz.schema.tables.SurveyQuestionListResponse existing_target_sqlr = SURVEY_QUESTION_LIST_RESPONSE.as("existing_target_sqlr");

        SelectConditionStep<Record6<Long, Long, String, Integer, Long, String>> responsesToInsert = tx
                .select(
                        targetInstanceIds.field(si.ID).as("siId"),
                        sqlr.QUESTION_ID,
                        sqlr.RESPONSE,
                        sqlr.POSITION,
                        sqlr.ENTITY_ID,
                        sqlr.ENTITY_KIND)
                .from(sqlr)
                .crossJoin(targetInstanceIds)
                .leftJoin(existing_target_sqlr)
                .on(sqlr.QUESTION_ID.eq(existing_target_sqlr.QUESTION_ID))
                .and(targetInstanceIds.field(si.ID).eq(existing_target_sqlr.SURVEY_INSTANCE_ID))
                .where(sqlr.SURVEY_INSTANCE_ID.eq(sourceSurveyInstanceId))
                .and(questionCondition)
                .and(existing_target_sqlr.SURVEY_INSTANCE_ID.isNull());

        return tx
                .insertInto(SURVEY_QUESTION_LIST_RESPONSE)
                .columns(
                        SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID,
                        SURVEY_QUESTION_LIST_RESPONSE.QUESTION_ID,
                        SURVEY_QUESTION_LIST_RESPONSE.RESPONSE,
                        SURVEY_QUESTION_LIST_RESPONSE.POSITION,
                        SURVEY_QUESTION_LIST_RESPONSE.ENTITY_ID,
                        SURVEY_QUESTION_LIST_RESPONSE.ENTITY_KIND)
                .select(responsesToInsert)
                .execute();
    }
}
