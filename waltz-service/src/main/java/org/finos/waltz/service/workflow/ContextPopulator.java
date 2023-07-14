package org.finos.waltz.service.workflow;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.finos.waltz.schema.tables.SurveyQuestionResponse;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record6;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.common.SetUtilities.union;
import static org.finos.waltz.model.EntityReference.mkRef;

@Service
public class ContextPopulator {

    private static final AssessmentRating ar = AssessmentRating.ASSESSMENT_RATING;
    private static final RatingSchemeItem rsi = RatingSchemeItem.RATING_SCHEME_ITEM;
    private static final AssessmentDefinition ad = AssessmentDefinition.ASSESSMENT_DEFINITION;
    private static final SurveyQuestionResponse sqr = SurveyQuestionResponse.SURVEY_QUESTION_RESPONSE;

    private static final Function<Record, AssessmentContextValue> ASSESSMENT_MAPPER = r -> ImmutableAssessmentContextValue
            .builder()
            .ratingName(r.get(rsi.NAME))
            .ratingCode(r.get(rsi.CODE))
            .ratingExternalId(r.get(rsi.EXTERNAL_ID))
            .ratingComment(r.get(ar.DESCRIPTION))
            .build();

    private static final Function<Record, SurveyQuestionResponseContextValue> QUESTION_MAPPER = r -> ImmutableSurveyQuestionResponseContextValue
            .builder()
            .value(r.get(sqr.STRING_RESPONSE))
            .comment(r.get(sqr.COMMENT))
            .build();

    private final DSLContext dsl;

    @Autowired
    public ContextPopulator(DSLContext dsl) {
        this.dsl = dsl;
    }


    public Set<ContextVariable<? extends ContextValue>> populateContext(Set<ContextVariableDeclaration> declarations,
                                                        GenericSelector selector)
    {
        Map<EntityKind, Collection<ContextVariableDeclaration>> declarationsByRefKind = groupBy(declarations, d -> d.ref().kind());

        Set<ContextVariable<AssessmentContextValue>> assessmentVariables = fetchAssessmentVariables(
                declarationsByRefKind.get(EntityKind.ASSESSMENT_DEFINITION),
                selector);

        Set<ContextVariable<SurveyQuestionResponseContextValue>> surveyResponseVariables = fetchSurveyResponseVariables(
                declarationsByRefKind.get(EntityKind.SURVEY_QUESTION),
                selector);

        return union(
                assessmentVariables,
                surveyResponseVariables);
    }


    private Set<ContextVariable<SurveyQuestionResponseContextValue>> fetchSurveyResponseVariables(Collection<ContextVariableDeclaration> declarations,
                                                                                                  GenericSelector selector) {
        if (isEmpty(declarations)) {
            return emptySet();
        } else {
            return emptySet();
        }
    }


    private Set<ContextVariable<AssessmentContextValue>> fetchAssessmentVariables(Collection<ContextVariableDeclaration> declarations,
                                                                  GenericSelector selector) {
        if (isEmpty(declarations)) {
            return emptySet();
        } else {
            Map<String, Collection<String>> extIdsToVarNames = groupBy(
                    declarations,
                    d -> d.ref().externalId(),
                    ContextVariableDeclaration::name);

            return dsl
                .fetch(prepareAssessmentQuery(declarations, selector))
                .stream()
                .flatMap(r -> extIdsToVarNames
                        .getOrDefault(r.get(ad.EXTERNAL_ID), Collections.emptySet())
                        .stream()
                        .map(varName -> ImmutableContextVariable
                            .<AssessmentContextValue>builder()
                            .name(varName)
                            .value(ASSESSMENT_MAPPER.apply(r))
                            .entityRef(mkRef(
                                    selector.kind(),
                                    r.get(ar.ENTITY_ID)))
                            .build()))
                .collect(Collectors.toSet());
        }
    }


    private SelectConditionStep<Record6<String, String, String, String, String, Long>> prepareAssessmentQuery(
            Collection<ContextVariableDeclaration> declarations,
            GenericSelector genericSelector)
    {
        Set<String> defExtIds = map(
                declarations,
                d -> d.ref().externalId());
        return DSL
                .select(rsi.EXTERNAL_ID,
                        rsi.NAME,
                        rsi.CODE,
                        ar.DESCRIPTION,
                        ad.EXTERNAL_ID,
                        ar.ENTITY_ID)
                .from(ar)
                .innerJoin(rsi).on(rsi.ID.eq(ar.RATING_ID))
                .innerJoin(ad).on(ad.ID.eq(ar.ASSESSMENT_DEFINITION_ID))
                .where(ad.EXTERNAL_ID.in(defExtIds))
                .and(ar.ENTITY_KIND.eq(genericSelector.kind().name()))
                .and(ar.ENTITY_ID.in(genericSelector.selector()));
    }

}
