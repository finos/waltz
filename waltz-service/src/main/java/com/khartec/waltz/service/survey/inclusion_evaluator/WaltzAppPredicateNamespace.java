package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class WaltzAppPredicateNamespace extends WaltzBasePredicateNamespace {

    private final DSLContext dsl;
    private final EntityReference subjectRef;


    public WaltzAppPredicateNamespace(DSLContext dsl,
                                      EntityReference subjectRef,
                                      List<SurveyQuestion> questions,
                                      Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        super(questions, responsesByQuestionId);
        this.dsl = dsl;
        this.subjectRef = subjectRef;
    }


    public boolean isRetiring() {
        Condition isPlanned = APPLICATION.PLANNED_RETIREMENT_DATE.isNotNull();

        Condition notRetiredYet = APPLICATION.ACTUAL_RETIREMENT_DATE.isNull()
                .or(APPLICATION.ACTUAL_RETIREMENT_DATE.greaterOrEqual(DSL.now()));

        return dsl
                .select(APPLICATION.PLANNED_RETIREMENT_DATE)
                .from(APPLICATION)
                .where(APPLICATION.ID.eq(subjectRef.id()))
                .and(isPlanned)
                .and(notRetiredYet)
                .fetch()
                .isNotEmpty();
    }

}
