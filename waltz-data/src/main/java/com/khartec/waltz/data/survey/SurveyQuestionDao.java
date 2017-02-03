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

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;

@Repository
public class SurveyQuestionDao {

    private static final RecordMapper<Record, SurveyQuestion> TO_DOMAIN_MAPPER = r -> {
        SurveyQuestionRecord record = r.into(SURVEY_QUESTION);
        return ImmutableSurveyQuestion.builder()
                .id(record.getId())
                .surveyTemplateId(record.getSurveyTemplateId())
                .questionText(record.getQuestionText())
                .helpText(record.getHelpText())
                .fieldType(SurveyQuestionFieldType.valueOf(record.getFieldType()))
                .sectionName(record.getSectionName())
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
        return dsl.select()
                .from(SURVEY_QUESTION)
                .where(SURVEY_QUESTION.SURVEY_TEMPLATE_ID.eq(templateId))
                .orderBy(SURVEY_QUESTION.POSITION.asc())
                .fetch(TO_DOMAIN_MAPPER);
    }
}
