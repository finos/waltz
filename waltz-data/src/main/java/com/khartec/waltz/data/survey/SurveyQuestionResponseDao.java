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

package com.khartec.waltz.data.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.ImmutableSurveyInstanceQuestionResponse;
import com.khartec.waltz.model.survey.ImmutableSurveyQuestionResponse;
import com.khartec.waltz.model.survey.SurveyInstanceQuestionResponse;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.schema.tables.records.SurveyQuestionResponseRecord;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.StringUtilities.ifEmpty;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;
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
        return dsl.select(SURVEY_QUESTION_RESPONSE.fields())
                .select(entityNameField)
                .from(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstanceQuestionResponse> findForSurveyRun(long surveyRunId) {
        return dsl.select(SURVEY_QUESTION_RESPONSE.fields())
                .select(entityNameField)
                .from(SURVEY_QUESTION_RESPONSE)
                .innerJoin(SURVEY_INSTANCE)
                .on(SURVEY_INSTANCE.ID.eq(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public void saveResponse(SurveyInstanceQuestionResponse response) {
        checkNotNull(response, "response cannot be null");
        checkNotNull(response.questionResponse(), "response.questionResponse() cannot be null");

        SurveyQuestionResponseRecord record = mkRecord(response);

        Condition responseExistsCondition = exists(selectFrom(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(response.surveyInstanceId())
                        .and(SURVEY_QUESTION_RESPONSE.QUESTION_ID.eq(response.questionResponse().questionId()))));

        Boolean responseExists = dsl.select(when(responseExistsCondition, true).otherwise(false))
                .fetchOne(Record1::value1);

        if (responseExists) {
            dsl.executeUpdate(record);
        } else {
            dsl.executeInsert(record);
        }
    }


    public int[] cloneResponses(long sourceSurveyInstanceId, long targetSurveyInstanceId) {
        Result<SurveyQuestionResponseRecord> records = dsl.select(SURVEY_QUESTION_RESPONSE.fields())
                .select(entityNameField)
                .from(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(sourceSurveyInstanceId))
                .fetchInto(SURVEY_QUESTION_RESPONSE);

        records.stream()
                .forEach(r -> r.setSurveyInstanceId(targetSurveyInstanceId));

        return dsl.batchInsert(records)
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
        record.setEntityResponseId(entityResponse.map(er -> er.id()).orElse(null));

        return record;
    }
}
