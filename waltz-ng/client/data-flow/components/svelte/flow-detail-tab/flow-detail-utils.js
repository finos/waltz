import _ from "lodash";

export function mkFlowDetails(flowView) {

    const ratingsByFlowId = _.groupBy(flowView.flowRatings, d => d.entityReference.id);
    const ratingSchemeItemsById = _.keyBy(flowView.ratingSchemeItems, d => d.id);
    const decoratorsByFlowId = _.groupBy(flowView.dataTypeDecorators, d => d.dataFlowId);
    const specsById = _.keyBy(flowView.physicalSpecifications, d => d.id);
    const logicalFlowsById = _.keyBy(flowView.flows, d => d.id);

    return _
        .chain(flowView.physicalFlows)
        .map(d => {

            const logicalFlow = _.get(logicalFlowsById, d.logicalFlowId);
            const assessmentRatingsForLogicalFlow = _.get(ratingsByFlowId, d.logicalFlowId, []);
            const dataTypesForLogicalFlow = _.get(decoratorsByFlowId, d.logicalFlowId, []);
            const specification = _.get(specsById, d.specificationId);

            const assessmentRatings = _.map(
                assessmentRatingsForLogicalFlow,
                d => ({definitionId: d.assessmentDefinitionId, ratingId: d.ratingId}));

            const ratingsByDefId = _
                .chain(assessmentRatingsForLogicalFlow)
                .groupBy(r => r.assessmentDefinitionId)
                .mapValues(v => _
                    .chain(v)
                    .map(r => ratingSchemeItemsById[r.ratingId])
                    .filter(d => d != null)
                    .sortBy(r => r.position, r => r.name)
                    .value())
                .value();

            return {
                logicalFlow,
                ratingsByDefId,
                dataTypesForLogicalFlow,
                physicalFlow: d,
                specification,
                assessmentRatings
            };
        })
        .sortBy(d => d.logicalFlow.target.name, d => d.logicalFlow.source.name)
        .value();
}

export function mkAssessmentFilters(flowView) {

    const ratingSchemeItemsById = _.keyBy(flowView.ratingSchemeItems, d => d.id);

    const ratingsByDefinitionId = _
        .chain(flowView.flowRatings)
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
        .chain(flowView.primaryAssessmentDefinitions)
        .map(d => Object.assign({}, {definition: d, ratings: _.get(ratingsByDefinitionId, d.id, [])}))
        .filter(d => !_.isEmpty(d.ratings))
        .value();
}

export function mkDefinitionFilterId(definitionId) {
    return `ASSESSMENT_DEFINITION_${definitionId}`;
}

export function mkDataTypeFilterId() {
    return "DATA_TYPE";
}

export function mkAssessmentFilter(filterId, newRatings) {
    return {
        id: filterId,
        ratings: newRatings,
        test: (r) => _.isEmpty(newRatings)
            ? x => true
            : _.some(newRatings, x => _.some(r.assessmentRatings, d => _.isEqual(x, d)))
    };
}

export function mkDataTypeFilter(filterId, dataTypes) {
    return {
        id: filterId,
        dataTypes,
        test: (r) => _.isEmpty(dataTypes)
            ? x => true
            : _.some(r.dataTypesForLogicalFlow, x => _.every(dataTypes, d => !_.isEqual(d, x.decoratorEntity.id)))
    };
}

export function mkLogicalFromFlowDetails(d) {
    return _.pick(d, ["logicalFlow", "ratingsByDefId", "dataTypesForLogicalFlow", "assessmentRatings"]);
}