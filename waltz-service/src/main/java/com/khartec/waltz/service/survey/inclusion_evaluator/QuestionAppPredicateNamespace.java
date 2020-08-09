package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.schema.tables.DataType;
import com.khartec.waltz.schema.tables.DataTypeUsage;
import com.khartec.waltz.schema.tables.EntityHierarchy;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;

public class QuestionAppPredicateNamespace extends QuestionEntityPredicateNamespace {

    public QuestionAppPredicateNamespace(DSLContext dsl,
                                         EntityReference subjectRef,
                                         List<SurveyQuestion> questions,
                                         Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        super(dsl, subjectRef, questions, responsesByQuestionId);
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


    public boolean hasDataType(String name){
        return ! dataTypeUsages(name).isEmpty();
    }


    public Set<String> dataTypeUsages(String name){

        DataTypeUsage dtu = DATA_TYPE_USAGE.as("dtu");
        DataType dt = DATA_TYPE.as("dt");
        EntityHierarchy eh = ENTITY_HIERARCHY.as("eh");

        Condition dtNameMatches = dt.CODE.eq(name)
                .or(dt.NAME.eq(name));

        Condition subjectMatches = dtu.ENTITY_ID.eq(subjectRef.id())
                .and(dtu.ENTITY_KIND.eq(subjectRef.kind().name()));

        return dsl
                .select(dtu.USAGE_KIND)
                .from(dt)
                .innerJoin(eh)
                .on(eh.ANCESTOR_ID.eq(dt.ID).and(eh.KIND.eq(EntityKind.DATA_TYPE.name())))
                .innerJoin(dtu)
                .on(dtu.DATA_TYPE_ID.eq(eh.ID))
                .where(dtNameMatches)
                .and(subjectMatches)
                .fetchSet(dtu.USAGE_KIND);
    }

}
