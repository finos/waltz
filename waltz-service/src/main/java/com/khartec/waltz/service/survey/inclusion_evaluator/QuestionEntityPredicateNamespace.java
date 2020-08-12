package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.schema.Tables.*;

/**
 *
 * NOTE: methods in this class may show as unused.  This is to be expected as they are referred to via
 * predicates in survey questions
 */
public class QuestionEntityPredicateNamespace extends QuestionBasePredicateNamespace {

    protected final DSLContext dsl;
    protected final EntityReference subjectRef;

    public QuestionEntityPredicateNamespace(DSLContext dsl,
                                            EntityReference subjectRef,
                                            List<SurveyQuestion> questions,
                                            Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        super(questions, responsesByQuestionId);
        this.dsl = dsl;
        this.subjectRef = subjectRef;
    }


    public String assessmentRating(String name, String defaultVal) {
        String result = dsl
                .select(RATING_SCHEME_ITEM.CODE)
                .from(ASSESSMENT_DEFINITION)
                .innerJoin(ASSESSMENT_RATING).on(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(ASSESSMENT_DEFINITION.ID))
                .innerJoin(RATING_SCHEME).on(RATING_SCHEME.ID.eq(ASSESSMENT_DEFINITION.RATING_SCHEME_ID))
                .innerJoin(RATING_SCHEME_ITEM).on(RATING_SCHEME_ITEM.ID.eq(ASSESSMENT_RATING.RATING_ID))
                .where(ASSESSMENT_DEFINITION.EXTERNAL_ID.eq(name).or(ASSESSMENT_DEFINITION.NAME.eq(name)))
                .and(ASSESSMENT_RATING.ENTITY_KIND.eq(subjectRef.kind().name()))
                .and(ASSESSMENT_RATING.ENTITY_ID.eq(subjectRef.id()))
                .fetchOne(RATING_SCHEME_ITEM.CODE);

        return Optional
                .ofNullable(result)
                .orElse(defaultVal);
    }


    public String assessmentRating(String name) {
        return assessmentRating(name, null);
    }


}
