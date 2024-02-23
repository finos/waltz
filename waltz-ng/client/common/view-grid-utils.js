import _ from "lodash";
import {FilterKinds} from "../data-flow/components/svelte/flow-detail-tab/filters/filter-utils";

export function prepareFlowClassificationsView(flowClassificationsView) {
    const classificationsById = _.keyBy(flowClassificationsView.flowClassifications, d => d.id);
    const dataTypesById = _.keyBy(flowClassificationsView.dataTypes, d => d.id);
    return _.map(flowClassificationsView.flowClassificationRules, d => {

        return {
            flowClassificationRule: d,
            flowClassification: classificationsById[d.classificationId],
            dataType: dataTypesById[d.dataTypeId]
        }
    })
}

export function prepareAssessmentsView(assessmentsView) {
    const definitionsById = _.keyBy(assessmentsView.assessmentDefinitions, d => d.id);
    const ratingSchemeItemsById = assessmentsView.ratingSchemeItemsById;
    return _.map(assessmentsView.assessmentRatings, d => {

        return {
            assessmentRating: d,
            assessmentDefinition: definitionsById[d.assessmentDefinitionId],
            ratingSchemeItem: ratingSchemeItemsById[d.ratingId]
        }
    })
}

export function mkDefinitionKey(def) {
    return `assessment_definition/${def.id}`;
}

export function mkAssessmentViewFilter(id,
                                       desiredRatings = [],
                                       ratingsProvider) {
    return {
        id,
        kind: FilterKinds.ASSESSMENT,
        ratings: desiredRatings,
        test: (row) => _.isEmpty(desiredRatings)
            ? x => true
            : _.some(
                desiredRatings,
                desiredRating => _.some(
                    ratingsProvider(row),
                    viewItem => _.isEqual(desiredRating.ratingId, viewItem.assessmentRating.ratingId)))
    };
}

export function mkClassificationViewFilter(id,
                                           desiredClassificationRatings = [],
                                           classificationProvider = d => d.flowClassification) {
    return {
        id,
        kind: FilterKinds.FLOW_CLASSIFICATION,
        classifications: desiredClassificationRatings,
        test: row => _.isEmpty(desiredClassificationRatings)
            ? true
            : _.some(
                desiredClassificationRatings,
                d => _.isEqual(_.get(classificationProvider(row), "code"), d))
    }
}
