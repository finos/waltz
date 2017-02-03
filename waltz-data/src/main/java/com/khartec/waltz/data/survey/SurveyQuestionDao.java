package com.khartec.waltz.data.survey;

import com.khartec.waltz.model.survey.ImmutableSurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionFieldType;
import com.khartec.waltz.schema.tables.records.SurveyQuestionRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.schema.tables.SurveyQuestion.SURVEY_QUESTION;

@Repository
public class SurveyQuestionDao {

    private static final RecordMapper<SurveyQuestionRecord, SurveyQuestion> TO_DOMAIN_MAPPER = r -> ImmutableSurveyQuestion.builder()
            .id(r.getId())
            .surveyTemplateId(r.getSurveyTemplateId())
            .questionText(r.getQuestionText())
            .helpText(Optional.ofNullable(r.getHelpText()))
            .fieldType(SurveyQuestionFieldType.valueOf(r.getFieldType()))
            .sectionName(Optional.ofNullable(r.getSectionName()))
            .position(r.getPosition())
            .isMandatory(r.getIsMandatory())
            .allowComment(r.getAllowComment())
            .build();


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
}
