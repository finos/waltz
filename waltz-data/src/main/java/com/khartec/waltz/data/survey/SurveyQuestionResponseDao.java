package com.khartec.waltz.data.survey;


import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.model.survey.ImmutableSurveyQuestionResponse;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.model.survey.SurveyQuestionResponseChange;
import com.khartec.waltz.schema.tables.records.SurveyQuestionResponseRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;
import static java.util.stream.Collectors.toList;

@Repository
public class SurveyQuestionResponseDao {

    private static final RecordMapper<Record, SurveyQuestionResponse> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionResponseRecord record = r.into(SURVEY_QUESTION_RESPONSE);

        return ImmutableSurveyQuestionResponse.builder()
                .id(record.getId())
                .surveyInstanceId(record.getSurveyInstanceId())
                .questionId(record.getQuestionId())
                .personId(record.getPersonId())
                .comment(Optional.ofNullable(record.getComment()))
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .stringResponse(Optional.ofNullable(record.getStringResponse()))
                .numberResponse(Optional.ofNullable(record.getNumberResponse()).map(BigDecimal::doubleValue))
                .booleanResponse(Optional.ofNullable(record.getBooleanResponse()))
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyQuestionResponseDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<SurveyQuestionResponse> findForInstance(long surveyInstanceId) {
        return dsl.selectFrom(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyQuestionResponse> findForSurveyRun(long surveyRunId) {
        return dsl.select(SURVEY_QUESTION_RESPONSE.fields())
                .from(SURVEY_QUESTION_RESPONSE)
                .innerJoin(SURVEY_INSTANCE)
                .on(SURVEY_INSTANCE.ID.eq(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public void insertQuestionResponses(long personId,
                                        long instanceId,
                                        List<SurveyQuestionResponseChange> inserts) {

        List<SurveyQuestionResponseRecord> records =
                inserts.stream()
                        .map(i -> mkRecord(personId, instanceId, i, DateTimeUtilities.nowUtc()))
                        .collect(toList());

        dsl.batchInsert(records)
                .execute();
    }


    public void updateQuestionResponses(long personId,
                                        long instanceId,
                                        List<SurveyQuestionResponseChange> updates) {

        List<SurveyQuestionResponseRecord> records =
                updates.stream()
                        .map(i -> mkRecord(personId, instanceId, i, DateTimeUtilities.nowUtc()))
                        .collect(toList());

        dsl.batchUpdate(records)
                .execute();
    }


    private SurveyQuestionResponseRecord mkRecord(long personId,
                                                  long instanceId,
                                                  SurveyQuestionResponseChange change,
                                                  LocalDateTime updatedAt) {
        SurveyQuestionResponseRecord record = new SurveyQuestionResponseRecord();
        change.id().ifPresent(record::setId);
        record.setSurveyInstanceId(instanceId);
        record.setQuestionId(change.questionId());
        record.setPersonId(personId);
        record.setComment(change.comment().orElse(null));
        record.setLastUpdatedAt(Timestamp.valueOf(updatedAt));
        record.setStringResponse(change.stringResponse().orElse(null));
        record.setNumberResponse(change.numberResponse().map(BigDecimal::valueOf).orElse(null));
        record.setBooleanResponse(change.booleanResponse().orElse(null));

        return record;
    }
}
