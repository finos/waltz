package com.khartec.waltz.data.survey;


import com.khartec.waltz.model.survey.ImmutableSurveyInstanceQuestionResponse;
import com.khartec.waltz.model.survey.ImmutableSurveyQuestionResponse;
import com.khartec.waltz.model.survey.SurveyInstanceQuestionResponse;
import com.khartec.waltz.schema.tables.records.SurveyQuestionResponseRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.tables.SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;

@Repository
public class SurveyQuestionResponseDao {

    private static final RecordMapper<Record, SurveyInstanceQuestionResponse> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionResponseRecord record = r.into(SURVEY_QUESTION_RESPONSE);

        return ImmutableSurveyInstanceQuestionResponse.builder()
                .surveyInstanceId(record.getSurveyInstanceId())
                .personId(record.getPersonId())
                .lastUpdatedAt(record.getLastUpdatedAt().toLocalDateTime())
                .questionResponse(ImmutableSurveyQuestionResponse.builder()
                        .id(record.getId())
                        .questionId(record.getQuestionId())
                        .comment(Optional.ofNullable(record.getComment()))
                        .stringResponse(Optional.ofNullable(record.getStringResponse()))
                        .numberResponse(Optional.ofNullable(record.getNumberResponse()).map(BigDecimal::doubleValue))
                        .booleanResponse(Optional.ofNullable(record.getBooleanResponse()))
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
        return dsl.selectFrom(SURVEY_QUESTION_RESPONSE)
                .where(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyInstanceQuestionResponse> findForSurveyRun(long surveyRunId) {
        return dsl.select(SURVEY_QUESTION_RESPONSE.fields())
                .from(SURVEY_QUESTION_RESPONSE)
                .innerJoin(SURVEY_INSTANCE)
                .on(SURVEY_INSTANCE.ID.eq(SURVEY_QUESTION_RESPONSE.SURVEY_INSTANCE_ID))
                .where(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(surveyRunId))
                .fetch(TO_DOMAIN_MAPPER);
    }


    public long saveResponse(SurveyInstanceQuestionResponse response) {
        checkNotNull(response, "response cannot be null");
        checkNotNull(response.questionResponse(), "response.questionResponse() cannot be null");

        SurveyQuestionResponseRecord record = mkRecord(response);

        if (response.questionResponse().id().isPresent()) {
            record.update();
        } else {
            record.insert();
        }

        return record.getId();
    }


    private SurveyQuestionResponseRecord mkRecord(SurveyInstanceQuestionResponse response) {
        SurveyQuestionResponseRecord record = dsl.newRecord(SURVEY_QUESTION_RESPONSE);;

        response.questionResponse().id().ifPresent(record::setId);
        record.setSurveyInstanceId(response.surveyInstanceId());
        record.setQuestionId(response.questionResponse().questionId());
        record.setPersonId(response.personId());
        record.setLastUpdatedAt(Timestamp.valueOf(response.lastUpdatedAt()));
        record.setComment(response.questionResponse().comment().orElse(null));
        record.setStringResponse(response.questionResponse().stringResponse().orElse(null));
        record.setNumberResponse(response.questionResponse().numberResponse().map(BigDecimal::valueOf).orElse(null));
        record.setBooleanResponse(response.questionResponse().booleanResponse().orElse(null));

        return record;
    }
}
