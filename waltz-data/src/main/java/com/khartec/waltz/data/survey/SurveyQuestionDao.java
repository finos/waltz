package com.khartec.waltz.data.survey;

import com.khartec.waltz.model.survey.ImmutableSurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionFieldType;
import com.khartec.waltz.schema.tables.records.SurveyQuestionRecord;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.Tables.SURVEY_INSTANCE;
import static com.khartec.waltz.schema.Tables.SURVEY_RUN;
import static com.khartec.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;

@Repository
public class SurveyQuestionDao {

    private static final RecordMapper<Record, SurveyQuestion> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionRecord record = r.into(SURVEY_QUESTION);
        return ImmutableSurveyQuestion.builder()
                .id(record.getId())
                .surveyTemplateId(record.getSurveyTemplateId())
                .questionText(record.getQuestionText())
                .helpText(Optional.ofNullable(record.getHelpText()))
                .fieldType(SurveyQuestionFieldType.valueOf(record.getFieldType()))
                .sectionName(Optional.ofNullable(record.getSectionName()))
                .position(record.getPosition())
                .isMandatory(record.getIsMandatory())
                .allowComment(record.getAllowComment())
                .build();
    };


    private final DSLContext dsl;


    @Autowired
    public SurveyQuestionDao(DSLContext dsl) {
        checkNotNull(dsl, "dsl cannot be null");

        this.dsl = dsl;
    }


    public List<SurveyQuestion> findForTemplate(long templateId) {
        return dsl.selectFrom(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.eq(templateId))
                .orderBy(SURVEY_QUESTION.POSITION.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }


    public List<SurveyQuestion> findForSurveyInstance(long surveyInstanceId) {
        return dsl.select(SURVEY_QUESTION.fields())
                .from(SURVEY_QUESTION)
                .innerJoin(SURVEY_RUN)
                .on(SURVEY_RUN.SURVEY_TEMPLATE_ID.eq(SURVEY_QUESTION.SURVEY_TEMPLATE_ID))
                .innerJoin(SURVEY_INSTANCE)
                .on(SURVEY_INSTANCE.SURVEY_RUN_ID.eq(SURVEY_RUN.ID))
                .where(SURVEY_INSTANCE.ID.eq(surveyInstanceId))
                .fetch(TO_DOMAIN_MAPPER);
    }
}
