package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationRuleDao;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRule;
import org.finos.waltz.model.flow_classification_rule.FlowClassificationRuleView;
import org.finos.waltz.model.flow_classification_rule.ImmutableFlowClassificationRuleView;
import org.finos.waltz.model.rating.RatingSchemeItem;
import org.finos.waltz.model.utils.IdUtilities;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FlowClassificationRuleViewService {

    private final FlowClassificationRuleDao flowClassificationRuleDao;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final AssessmentRatingDao assessmentRatingDao;
    private final RatingSchemeDAO ratingSchemeDAO;


    public FlowClassificationRuleViewService(FlowClassificationRuleDao flowClassificationRuleDao,
                                             AssessmentDefinitionDao assessmentDefinitionDao, AssessmentRatingDao assessmentRatingDao, RatingSchemeDAO ratingSchemeDAO) {
        this.flowClassificationRuleDao = flowClassificationRuleDao;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.assessmentRatingDao = assessmentRatingDao;
        this.ratingSchemeDAO = ratingSchemeDAO;
    }


    public FlowClassificationRuleView findAll() {
        List<FlowClassificationRule> allRules = flowClassificationRuleDao.findAll();

        Set<AssessmentDefinition> primaryAssessmentDefinition = assessmentDefinitionDao
                .findPrimaryDefinitionsForKind(
                    EntityKind.FLOW_CLASSIFICATION_RULE,
                    Optional.empty());

        Set<Long> primaryDefIds = IdUtilities.toIds(primaryAssessmentDefinition);

        Set<AssessmentRating> assessmentRatings = assessmentRatingDao
                .findByEntityKind(
                    EntityKind.FLOW_CLASSIFICATION_RULE,
                    Optional.empty())
                .stream()
                .filter(r -> primaryDefIds.contains(r.assessmentDefinitionId()))
                .collect(Collectors.toSet());

        Set<Long> ratingSchemeItemIds = SetUtilities.map(assessmentRatings, AssessmentRating::ratingId);
        Set<RatingSchemeItem> ratingSchemeItems = ratingSchemeDAO.findRatingSchemeItemsByIds(ratingSchemeItemIds);

        return ImmutableFlowClassificationRuleView
                .builder()
                .flowClassificationRules(allRules)
                .primaryAssessmentDefinitions(primaryAssessmentDefinition)
                .assessmentRatings(assessmentRatings)
                .ratingSchemeItems(ratingSchemeItems)
                .build();
    }
}
