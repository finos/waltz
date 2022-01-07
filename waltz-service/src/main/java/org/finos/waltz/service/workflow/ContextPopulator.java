package org.finos.waltz.service.workflow;

import org.finos.waltz.common.CollectionUtilities;
import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.AssessmentRating;
import org.finos.waltz.schema.tables.RatingSchemeItem;
import org.jooq.Record5;
import org.jooq.Select;
import org.jooq.impl.DSL;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.finos.waltz.common.MapUtilities.groupBy;
import static org.finos.waltz.common.SetUtilities.map;

public class ContextPopulator {

    private static final AssessmentRating ar = AssessmentRating.ASSESSMENT_RATING;
    private static final RatingSchemeItem rsi = RatingSchemeItem.RATING_SCHEME_ITEM;
    private static final AssessmentDefinition ad = AssessmentDefinition.ASSESSMENT_DEFINITION;

    public void populateContext(Set<WorkflowContextVariableDeclaration> declarations, GenericSelector selector) {

        Map<EntityKind, Collection<WorkflowContextVariableDeclaration>> declarationsByRefKind = groupBy(declarations, d -> d.ref().kind());

        Select<Record5<String, String, String, String, Long>> assessmentCall = prepareAssessmentQuery(declarationsByRefKind.get(EntityKind.ASSESSMENT_DEFINITION), selector);

//        if (assessmentCall != null) {
//            assessmentCall
//                    .fetch()
//                    .stream()
//                    .map(r -> {
//
//                    })
//                    .collect(Collectors.toSet());
//        }

        Select<Record5<String, String, String, String, Long>> surveyCall = prepareAssessmentQuery(declarationsByRefKind.get(EntityKind.SURVEY_QUESTION), selector);

    }

    private Select<Record5<String, String, String, String, Long>> prepareAssessmentQuery(
            Collection<WorkflowContextVariableDeclaration> declarations,
            GenericSelector genericSelector)
    {
        if (CollectionUtilities.isEmpty(declarations)) {
            return null;
        } else {
            Set<String> defExtIds = map(
                    declarations,
                    d -> d.ref().externalId());
            return DSL
                    .select(rsi.EXTERNAL_ID,
                            rsi.NAME,
                            rsi.CODE,
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

}
