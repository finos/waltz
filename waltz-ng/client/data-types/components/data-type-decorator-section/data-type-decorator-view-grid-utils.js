import _ from "lodash";
import {
    mkLastUpdatedFormatter,
    mkNameFormatter,
    mkPrimaryAssessmentAndCategoryColumns,
    mkRatingSchemeItemFormatter,
    mkReadOnlyFormatter
} from "../../../common/slick-grid-utils";
import {cmp, compareDates} from "../../../common/sort-utils";
import {mkDefinitionKey, prepareAssessmentsView, prepareFlowClassificationsView} from "../../../common/view-grid-utils";

const baseColumns = [
    {
        id: "read_only",
        name: "",
        field: "isReadonly",
        width: 60,
        sortable:  false,
        formatter: mkReadOnlyFormatter(),
        sortFn: (a, b) => cmp(a?.decoratorEntity.name, b?.decoratorEntity.name)
    },
    {
        id: "data_type",
        name: "Data Type",
        field: "decoratorEntity",
        sortable:  true,
        width: 180,
        formatter: mkNameFormatter(),
        sortFn: (a, b) => cmp(a?.decoratorEntity.name, b?.decoratorEntity.name)
    },
    {
        id: "source_outbound_classification",
        name: "Source Outbound Classification",
        field: "sourceOutboundClassification",
        sortable:  true,
        width: 180,
        formatter: mkRatingSchemeItemFormatter(d => d.name, d => d),
        sortFn: (a, b) => cmp(a?.sourceOutboundClassification.name, b?.sourceOutboundClassification.name)
    },
    {
        id: "target_inbound_classification",
        name: "Target Inbound Classification",
        field: "targetInboundClassification",
        sortable:  true,
        width: 180,
        formatter: mkRatingSchemeItemFormatter(d => d.name, d => d),
        sortFn: (a, b) => cmp(a?.targetInboundClassification.name, b?.targetInboundClassification.name)
    }
];


const lastUpdatedAndProvenanceColumns = [
    {
        id: "last_updated_by",
        name: "Last Updated By",
        field: "lastUpdatedBy",
        sortable:  true,
        width: 180,
        sortFn: (a, b) => cmp(a?.decoratorEntity.name, b?.decoratorEntity.name)
    },
    {
        id: "last_updated_at",
        name: "Last Updated At",
        field: "lastUpdatedAt",
        sortable:  true,
        width: 180,
        formatter: mkLastUpdatedFormatter(),
        sortFn: (a, b) => compareDates(a?.lastUpdatedAt, b?.lastUpdatedAt)
    },
    {
        id: "provenance",
        name: "Provenance",
        field: "provenance",
        width: 180,
        sortable:  true,
        sortFn: (a, b) => cmp(a?.provenance, b?.provenance)
    }
];


export function prepareData($viewData) {

    const enrichedPrimaryAssessments = prepareAssessmentsView($viewData.primaryAssessments);
    const assessmentRatingsByDecoratorId = _.groupBy(enrichedPrimaryAssessments, d => d.assessmentRating.entityReference.id);

    const enrichedClassificationRules = prepareFlowClassificationsView($viewData.flowClassificationRules);
    const flowClassificationRulesById = _.keyBy(enrichedClassificationRules, d => d.flowClassificationRule.id);
    const classificationsByCode = _.keyBy($viewData.classifications, d => d.code);

    return _
        .chain($viewData.dataTypeDecorators)
        .map(d => {

            const ratingsForDecorator = _.get(assessmentRatingsByDecoratorId, d.id, []);
            const sourceOutboundClassification = _.get(classificationsByCode, d.rating);
            const targetInboundClassification = _.get(classificationsByCode, d.targetInboundRating);

            const ratingsByDefnId = _
                .chain(ratingsForDecorator)
                .reduce((acc, d) => {
                        const key = mkDefinitionKey(d.assessmentDefinition);
                        const values = acc[key] || [];
                        values.push(d);
                        acc[key] = values;
                        return acc;
                    },
                    {})
                .value();

            const sourceOutboundRule = _.get(flowClassificationRulesById, d.flowClassificationRuleId);
            const targetInboundRule = _.get(flowClassificationRulesById, d.inboundFlowClassificationRuleId);

            return Object.assign(
                {},
                d,
                {
                    ...ratingsByDefnId,
                    sourceOutboundClassification,
                    targetInboundClassification,
                    sourceOutboundRule,
                    targetInboundRule,
                    assessmentRatings: ratingsForDecorator
            });
        })
        .value();
}


export function mkColumns(assessmentDefinitions = []) {
    return _.concat(
        baseColumns,
        mkPrimaryAssessmentAndCategoryColumns(assessmentDefinitions, []),
        lastUpdatedAndProvenanceColumns);
}


export function getAssessmentViewFilters(assessmentsView) {

    const ratingSchemeItemsById = _.keyBy(assessmentsView.ratingSchemeItems, d => d.id);

    const ratingsByDefinitionId = _
        .chain(assessmentsView.assessmentRatings)
        .groupBy(r => r.assessmentDefinitionId)
        .mapValues(v => _
            .chain(v)
            .map(r => ratingSchemeItemsById[r.ratingId])
            .filter(d => d != null)
            .uniq()
            .sortBy(r => r.position, r => r.name)
            .value())
        .value();

    return _
        .chain(assessmentsView.assessmentDefinitions)
        .map(d => Object.assign({}, {definition: d, ratings: _.get(ratingsByDefinitionId, d.id, [])}))
        .filter(d => !_.isEmpty(d.ratings))
        .value();
}