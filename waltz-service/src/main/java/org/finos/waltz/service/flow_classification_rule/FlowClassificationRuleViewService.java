package org.finos.waltz.service.flow_classification_rule;

import org.finos.waltz.common.SetUtilities;
import org.finos.waltz.data.assessment_definition.AssessmentDefinitionDao;
import org.finos.waltz.data.assessment_rating.AssessmentRatingDao;
import org.finos.waltz.data.data_type.DataTypeDao;
import org.finos.waltz.data.flow_classification_rule.FlowClassificationDao;
import org.finos.waltz.data.rating_scheme.RatingSchemeDAO;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.IdSelectionOptions;
import org.finos.waltz.model.assessment_definition.AssessmentDefinition;
import org.finos.waltz.model.assessment_rating.AssessmentRating;
import org.finos.waltz.model.datatype.DataType;
import org.finos.waltz.model.flow_classification.FlowClassification;
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

    private final FlowClassificationRuleService flowClassificationRuleService;
    private final AssessmentDefinitionDao assessmentDefinitionDao;
    private final AssessmentRatingDao assessmentRatingDao;
    private final RatingSchemeDAO ratingSchemeDAO;
    private final DataTypeDao dataTypeDao;
    private final FlowClassificationDao flowClassificationDao;


    public FlowClassificationRuleViewService(FlowClassificationRuleService flowClassificationRuleService,
                                             AssessmentDefinitionDao assessmentDefinitionDao,
                                             AssessmentRatingDao assessmentRatingDao,
                                             RatingSchemeDAO ratingSchemeDAO,
                                             DataTypeDao dataTypeDao,
                                             FlowClassificationDao flowClassificationDao) {
        this.flowClassificationRuleService = flowClassificationRuleService;
        this.assessmentDefinitionDao = assessmentDefinitionDao;
        this.assessmentRatingDao = assessmentRatingDao;
        this.ratingSchemeDAO = ratingSchemeDAO;
        this.dataTypeDao = dataTypeDao;
        this.flowClassificationDao = flowClassificationDao;
    }


    public FlowClassificationRuleView getViewForSelector(IdSelectionOptions opts) {
        Set<FlowClassificationRule> rules = flowClassificationRuleService.findClassificationRules(opts);

        List<DataType> dataTypes = dataTypeDao.findByIds(SetUtilities.map(rules, FlowClassificationRule::dataTypeId));
        Set<FlowClassification> classifications = flowClassificationDao.findAll();

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

        Set<Long> ratingSchemeItemIds = SetUtilities.map(
                assessmentRatings,
                AssessmentRating::ratingId);

        Set<RatingSchemeItem> ratingSchemeItems = ratingSchemeDAO.findRatingSchemeItemsByIds(ratingSchemeItemIds);

        return ImmutableFlowClassificationRuleView
                .builder()
                .flowClassificationRules(rules)
                .flowClassifications(classifications)
                .dataTypes(dataTypes)
                .primaryAssessmentDefinitions(primaryAssessmentDefinition)
                .assessmentRatings(assessmentRatings)
                .ratingSchemeItems(ratingSchemeItems)
                .build();
    }

}
