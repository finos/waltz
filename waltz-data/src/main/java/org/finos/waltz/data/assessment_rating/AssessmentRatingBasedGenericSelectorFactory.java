package org.finos.waltz.data.assessment_rating;

import org.finos.waltz.data.GenericSelector;
import org.finos.waltz.data.GenericSelectorFactory;
import org.finos.waltz.model.AssessmentBasedSelectionFilter;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.jooq.Record1;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.finos.waltz.schema.Tables.ASSESSMENT_RATING;

@Service
public class AssessmentRatingBasedGenericSelectorFactory {


    public static Select<Record1<Long>> prepareFilteredSelection(EntityKind targetKind,
                                                                 IdSelectionOptions options,
                                                                 Optional<AssessmentBasedSelectionFilter> params) {

        GenericSelectorFactory genericSelectorFactory = new GenericSelectorFactory();
        GenericSelector genericSelector = genericSelectorFactory.applyForKind(targetKind, options);

        return params
                .map(p -> genericSelector.selector().intersect(mkAssessmentRatingSelector(p, genericSelector.kind())))
                .orElse(genericSelector.selector());
    }


    private static Select<? extends Record1<Long>> mkAssessmentRatingSelector(AssessmentBasedSelectionFilter params,
                                                                              EntityKind targetKind) {
        return DSL
                .select(ASSESSMENT_RATING.ENTITY_ID)
                .from(ASSESSMENT_RATING)
                .where(ASSESSMENT_RATING.ASSESSMENT_DEFINITION_ID.eq(params.definitionId())
                        .and(ASSESSMENT_RATING.RATING_ID.in(params.ratingIds())
                                .and(ASSESSMENT_RATING.ENTITY_KIND.eq(targetKind.name()))));
    }

}
