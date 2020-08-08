package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityLifecycleStatus;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.assessment_definition.AssessmentDefinition;
import com.khartec.waltz.model.assessment_definition.AssessmentVisibility;
import com.khartec.waltz.model.assessment_definition.ImmutableAssessmentDefinition;
import com.khartec.waltz.model.assessment_rating.AssessmentRating;
import com.khartec.waltz.model.assessment_rating.AssessmentRatingDetail;
import com.khartec.waltz.model.assessment_rating.ImmutableAssessmentRating;
import com.khartec.waltz.model.assessment_rating.ImmutableAssessmentRatingDetail;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.datatype.ImmutableDataType;
import com.khartec.waltz.model.rating.ImmutableRagName;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.khartec.waltz.common.DateTimeUtilities.toLocalDateTime;
import static com.khartec.waltz.common.StringUtilities.firstChar;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
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


    public boolean hasDatatypeByName(String name){

        List<DataType> datatypes = datatypes();

        long count = datatypes
                .stream()
                .filter(d -> d.name().equalsIgnoreCase(name))
                .count();

        return count > 0;
    }


    private List<DataType> datatypes(){

        return dsl
                .select()
                .from(DATA_TYPE)
                .innerJoin(LOGICAL_FLOW_DECORATOR).on(DATA_TYPE.ID.eq(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID)
                    .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name())))
                .innerJoin(LOGICAL_FLOW).on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .where((LOGICAL_FLOW.SOURCE_ENTITY_ID.eq(subjectRef.id())
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name())))
                .or(LOGICAL_FLOW.TARGET_ENTITY_ID.eq(subjectRef.id())
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))))
                .and(LOGICAL_FLOW.IS_REMOVED.isFalse()
                        .and(LOGICAL_FLOW.ENTITY_LIFECYCLE_STATUS.eq(EntityLifecycleStatus.ACTIVE.name())))
                .fetch(r -> ImmutableDataType.builder()
                        .code(r.get(DATA_TYPE.CODE))
                        .description(mkSafe(r.get(DATA_TYPE.DESCRIPTION)))
                        .name(r.get(DATA_TYPE.NAME))
                        .id(Optional.ofNullable(r.get(DATA_TYPE.ID)))
                        .parentId(Optional.ofNullable(r.get(DATA_TYPE.PARENT_ID)))
                        .deprecated(r.get(DATA_TYPE.DEPRECATED))
                        .concrete(r.get(DATA_TYPE.CONCRETE))
                        .unknown(r.get(DATA_TYPE.UNKNOWN))
                        .build());
    }

}
