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

package com.khartec.waltz.data.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.ImmutableSurveyInstanceQuestionResponse;
import com.khartec.waltz.model.survey.ImmutableSurveyQuestionResponse;
import com.khartec.waltz.model.survey.SurveyInstanceQuestionResponse;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.schema.Tables;
import com.khartec.waltz.schema.tables.records.SurveyQuestionListResponseRecord;
import com.khartec.waltz.schema.tables.records.SurveyQuestionResponseRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.common.CollectionUtilities.first;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.SetUtilities.map;
import static com.khartec.waltz.common.StringUtilities.ifEmpty;
import static com.khartec.waltz.common.StringUtilities.join;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_QUESTION_LIST_RESPONSE;
import static com.khartec.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.*;

@Repository
public class SurveyQuestionResponseDao {

    private static final Field<String> entityNameField = InlineSelectFieldFactory.mkNameField(
            SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_ID,
            SURVEY_QUESTION_RESPONSE.ENTITY_RESPONSE_KIND,
            newArrayList(EntityKind.APPLICATION, EntityKind.PERSON));

    private static final RecordMapper<Record, SurveyInstanceQuestionResponse> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionResponseRecord record = r.into(SURVEY_QUESTION_RESPONSE);

        Optional<EntityReference> entityReference = Optional.empty();
        if(record.getEntityResponseId() != null && record.getEntityResponseKind() != null) {
            entityReference = Optional.of(EntityReference.mkRef(
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
                        .comment(Optional.ofNullable(record.getComment()))
                        .stringResponse(Optional.ofNullable(record.getStringResponse()))
                        .numberResponse(Optional.ofNullable(record.getNumberResponse()).map(BigDecimal::doubleValue))
                        .booleanResponse(Optional.ofNullable(record.getBooleanResponse()))
                        .dateResponse(Optional.ofNullable(record.getDateResponse()).map(Date::toLocalDate))
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
        Map<Long, List<SurveyQuestionListResponseRecord>> questionIdToListResponses =
                dsl.selectFrom(SURVEY_QUESTION_LIST_RESPONSE)
                .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch()
                .stream()
                .collect(groupingBy(SurveyQuestionListResponseRecord::getQuestionId, toList()));

        // fetch responses
        List<SurveyInstanceQuestionResponse> responses = dsl.select(SURVEY_QUESTION_RESPONSE.fields())
                .select(entityNameField)
                .from(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);

        // plug list responses into responses
        return responses.stream()
                .map(r -> {
                    if (questionIdToListResponses.containsKey(r.questionResponse().questionId())) {
                        List<String> listResponse = questionIdToListResponses.get(r.questionResponse().questionId())
                                .stream()
                                .sorted(comparingInt(SurveyQuestionListResponseRecord::getPosition))
                                .map(SurveyQuestionListResponseRecord::getResponse)
                                .collect(toList());

                        return ImmutableSurveyInstanceQuestionResponse
                                .copyOf(r)
                                .withQuestionResponse(ImmutableSurveyQuestionResponse
                                                        .copyOf(r.questionResponse())
                                                        .withListResponse(listResponse));
                    } else {
                        return r;
                    }
                })
                .collect(toList());
    }


    public int deletePreviousResponse(List<SurveyInstanceQuestionResponse> previousResponses) {
        checkNotNull(previousResponses, "responses cannot be null");
        if (!previousResponses.isEmpty()) {
            Set<Long> instanceIds = map(
                    previousResponses,
                    SurveyInstanceQuestionResponse::surveyInstanceId);

            checkTrue(instanceIds.size() == 1, "All responses must for the same surveyInstance");
            final Long instanceId = first(previousResponses).surveyInstanceId();

            final Set<Long> previousResponseIds = map(
                    previousResponses,
                    qr -> qr.questionResponse().questionId());

            int rmSingleCount = dsl
                    .deleteFrom(Tables.SURVEY_QUESTION_RESPONSE)
                    .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(instanceId))
                    .and(SURVEY_QUESTION_RESPONSE.QUESTION_ID.in(previousResponseIds))
                    .execute();

            int rmListCount = dsl
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

        Condition responseExistsCondition = exists(selectFrom(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(response.surveyInstanceId())
                        .and(SURVEY_QUESTION_RESPONSE.QUESTION_ID.eq(response.questionResponse().questionId()))));

        // save survey_question_response record
        Boolean responseExists = dsl
                .select(when(responseExistsCondition, true)
                        .otherwise(false))
                .fetchOne(Record1::value1);

        dsl.transaction(configuration -> {
            DSLContext txDsl = DSL.using(configuration);

            if (responseExists) {
                txDsl.executeUpdate(record);
            } else {
                txDsl.executeInsert(record);
            }

            // save survey_question_list_response records
            response.questionResponse().listResponse().ifPresent(lrs -> {
                txDsl.deleteFrom(SURVEY_QUESTION_LIST_RESPONSE)
                        .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(response.surveyInstanceId()))
                        .and(SURVEY_QUESTION_LIST_RESPONSE.QUESTION_ID.eq(response.questionResponse().questionId()))
                        .execute();

                if (! lrs.isEmpty()) {
                    AtomicInteger counter = new AtomicInteger(0);
                    List<SurveyQuestionListResponseRecord> listResponses =
                            lrs.stream()
                            .map(lr -> {
                                SurveyQuestionListResponseRecord rec = new SurveyQuestionListResponseRecord();
                                rec.setSurveyInstanceId(response.surveyInstanceId());
                                rec.setQuestionId(response.questionResponse().questionId());
                                rec.setResponse(lr);
                                rec.setPosition(counter.incrementAndGet());

                                return rec;
                            })
                            .collect(toList());

                    txDsl.batchInsert(listResponses)
                            .execute();
                }
            });
        });
    }


    public void cloneResponses(long sourceSurveyInstanceId, long targetSurveyInstanceId) {
        List<SurveyQuestionResponseRecord> responseRecords = dsl
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

        List<SurveyQuestionListResponseRecord> listResponseRecords = dsl
                .selectFrom(SURVEY_QUESTION_LIST_RESPONSE)
                .where(SURVEY_QUESTION_LIST_RESPONSE.SURVEY_INSTANCE_ID.eq(sourceSurveyInstanceId))
                .fetchInto(SURVEY_QUESTION_LIST_RESPONSE)
                .stream()
                .map(r -> {
                    r.setSurveyInstanceId(targetSurveyInstanceId);
                    r.changed(true);
                    return r;
                })
                .collect(toList());

        dsl.transaction(configuration -> {
            DSLContext txDsl = DSL.using(configuration);

            txDsl.batchInsert(responseRecords)
                    .execute();

            txDsl.batchInsert(listResponseRecords)
                    .execute();
        });
    }


    public int deleteForSurveyRun(long surveyRunId) {
        Select<Record1<Long>> surveyInstanceIdSelector = dsl.select(SURVEY_INSTANCE.ID)
                .from(SURVEY_INSTANCE)
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId));

        // this will also auto delete any survey_question_list_response records (fk delete cascade)
        return dsl.delete(SURVEY_QUESTION_RESPONSE)
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
                                        .map(l -> join(l, ";;"))
                                        .orElse(null));

        return record;
    }
}
