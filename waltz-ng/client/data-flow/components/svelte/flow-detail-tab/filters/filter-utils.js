import _ from "lodash";
import {Directions} from "../flow-detail-utils";

export const FilterKinds = {
    DIRECTION: "DIRECTION",
    DATA_TYPE: "DATA_TYPE",
    ASSESSMENT: "ASSESSMENT",
    PHYSICAL_FLOW_ATTRIBUTE: "PHYSICAL_FLOW_ATTRIBUTE",
    SELECTED_LOGICAL: "SELECTED_LOGICAL",
    FLOW_CLASSIFICATION: "FLOW_CLASSIFICATION"
}

export function getAssessmentFilters(flowView) {

    const ratingSchemeItemsById = _.keyBy(flowView.ratingSchemeItems, d => d.id);
    const definitions = _.compact(_.concat(
        flowView.logicalFlowAssessmentDefinitions,
        flowView.physicalFlowAssessmentDefinitions,
        flowView.physicalSpecificationAssessmentDefinitions));

    const ratings = _.compact(_.concat(
        flowView.logicalFlowRatings,
        flowView.physicalFlowRatings,
        flowView.physicalSpecificationRatings));

    const ratingsByDefinitionId = _
        .chain(ratings)
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
        .chain(definitions)
        .map(d => Object.assign({}, {definition: d, ratings: _.get(ratingsByDefinitionId, d.id, [])}))
        .filter(d => !_.isEmpty(d.ratings))
        .value();
}

export function mkCriticalityFilterId() {
    return "PHYSICAL_FLOW_CRITICALITY";
}

export function mkFrequencyFilterId() {
    return "PHYSICAL_FLOW_FREQUENCY";
}

export function mkTransportKindFilterId() {
    return "PHYSICAL_FLOW_TRANSPORT_KIND";
}

export function mkDefinitionFilterId(definitionId) {
    return `ASSESSMENT_DEFINITION_${definitionId}`;
}

export function mkClassificationFilterId() {
    return `FLOW_CLASSIFICATION`;
}

export function mkDataTypeFilterId() {
    return "DATA_TYPE";
}

export function mkDirectionFilterId() {
    return "FLOW_DIRECTION";
}

export function mkAssessmentFilter(id, desiredRatings) {
    return {
        id,
        kind: FilterKinds.ASSESSMENT,
        ratings: desiredRatings,
        test: (flowRow) => _.isEmpty(desiredRatings)
            ? x => true
            : _.some(
                desiredRatings,
                desiredRating => _.some(
                    flowRow.allRatings,
                    flowRating => _.isEqual(desiredRating.ratingId, flowRating.ratingId)))
    };
}

export function mkCriticalityFilter(id, criticalities) {
    return {
        id,
        kind: FilterKinds.PHYSICAL_FLOW_ATTRIBUTE,
        criticalities,
        test: (r) => _.isEmpty(criticalities)
            ? x => true
            : _.includes(criticalities, r.physicalFlow?.criticality)
    };
}

export function mkFrequencyFilter(id, frequencies) {
    return {
        id,
        kind: FilterKinds.PHYSICAL_FLOW_ATTRIBUTE,
        frequencies,
        test: (r) => _.isEmpty(frequencies)
            ? x => true
            : _.includes(frequencies, r.physicalFlow?.frequency)
    };
}

export function mkTransportKindFilter(id, transportKinds) {
    return {
        id,
        kind: FilterKinds.PHYSICAL_FLOW_ATTRIBUTE,
        transportKinds,
        test: (r) => _.isEmpty(transportKinds)
            ? x => true
            : _.includes(transportKinds, r.physicalFlow?.transport)
    };
}

export function mkDataTypeFilter(id, dataTypes) {
    return {
        id,
        kind: FilterKinds.DATA_TYPE,
        dataTypes,
        test: (r) => _.isEmpty(dataTypes)
            ? x => true
            : _.some(r.dataTypesForLogicalFlow, x => _.every(dataTypes, d => !_.isEqual(d, x.decoratorEntity.id)))
    };
}

export function mkDirectionFilter(id, direction) {
    return {
        id,
        kind: FilterKinds.DIRECTION,
        direction,
        test: (r) => direction === Directions.ALL
            ? true
            : _.isEqual(r.direction, direction)
    };
}

export function mkClassificationFilter(id, desiredClassificationRatings = []) {
    return {
        id,
        kind: FilterKinds.FLOW_CLASSIFICATION,
        classifications: desiredClassificationRatings,
        test: flowRow => _.isEmpty(desiredClassificationRatings)
            ? true
            : _.some(
                flowRow.dataTypesForLogicalFlow,
                x => _.some(
                    desiredClassificationRatings,
                    d => _.isEqual(d, x.rating)))
    }
}