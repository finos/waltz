package org.finos.waltz.data.assessment_rating;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.EntityKind;
import org.jooq.Condition;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.finos.waltz.common.CollectionUtilities.isEmpty;
import static org.finos.waltz.common.SetUtilities.map;
import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;

@Service
public class AssessmentRatingBasedGenericSelectorFactory {


    /**
     * Creates a selector for entity ids of the given kind. If there are filters these are applied, if the filters are provided
     * but there are no specified ratings, this uses the default generic selector.
     *
     * @param genericSelector the selector for the target entity
     * @param params          optional assessment based filter parameters to limit the selector by ratings for the entity
     * @return id selector
     */
    public static Select<Record1<Long>> applyFiltersToSelector(GenericSelector genericSelector,
                                                               Set<AssessmentBasedSelectionFilter> params) {
        if (isEmpty(params)) {
            return genericSelector.selector();
        } else {
            Set<Select<Record1<Long>>> assessmentRatingSelectors = map(
                    params,
                    d -> mkAssessmentRatingSelector(d, genericSelector.kind()));

            return assessmentRatingSelectors
                    .stream()
                    .reduce(genericSelector.selector(), Select::intersect);
        }
    }


    private static Select<Record1<Long>> mkAssessmentRatingSelector(AssessmentBasedSelectionFilter params,
                                                                    EntityKind targetKind) {

        Condition ratingIdCondition = !params.ratingIds().isEmpty() ? DSL.trueCondition() : ASSESSMENT_RATING.RATING_ID.in(params.ratingIds());
        return DSL
                .select(ASSESSMENT_RATING.ENTITY_ID)
                .from(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(params.definitionId())
                        .and(ratingIdCondition)
                        .and(ASSESSMENT_RATING.ENTITY_KIND.eq(targetKind.name())));
    }

}
