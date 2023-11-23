import _ from "lodash";
import {sameRef} from "../../../../common/entity-utils";

export const Directions = {
    INBOUND: "INBOUND",
    OUTBOUND: "OUTBOUND",
    ALL: "ALL"
}

function determineDirection(flow, parentEntityRef) {
    if (sameRef(flow.target, parentEntityRef)) {
        return Directions.INBOUND;
    } else {
        return Directions.OUTBOUND;
    }
}

export function mkFlowDetails(flowView, parentEntityRef) {

    const decoratorsByKind = _.groupBy(flowView.dataTypeDecorators, d => d.entityReference.kind);
    const logicalDecoratorsByFlowId = _.groupBy(decoratorsByKind['LOGICAL_DATA_FLOW'], d => d.dataFlowId);
    const specDecoratorsBySpecId = _.groupBy(decoratorsByKind['PHYSICAL_SPECIFICATION'], d => d.dataFlowId);
    const ratingsByFlowId = _.groupBy(flowView.flowRatings, d => d.entityReference.id);
    const ratingSchemeItemsById = _.keyBy(flowView.ratingSchemeItems, d => d.id);
    const specsById = _.keyBy(flowView.physicalSpecifications, d => d.id);
    const logicalFlowsById = _.keyBy(flowView.flows, d => d.id);
    const physicalFlowsByLogicalFlowId = _.groupBy(flowView.physicalFlows, d => d.logicalFlowId);

    return _
        .chain(flowView.flows)
        .flatMap(d => {
            const physicalFlows = _.get(physicalFlowsByLogicalFlowId, d.id, []);
            return _.isEmpty(physicalFlows)
                ? [{logicalFlow: d, physicalFlow: null}]
                : _.map(physicalFlows, p => ({logicalFlow: d, physicalFlow: p}))
        })
        .map(t => {

            const logicalFlow = t.logicalFlow;
            const physicalFlow = t.physicalFlow;

            const assessmentRatingsForLogicalFlow = _.get(ratingsByFlowId, logicalFlow.id, []);
            const dataTypesForLogicalFlow = _.get(logicalDecoratorsByFlowId, logicalFlow.id, []);
            const specification = _.get(specsById, physicalFlow?.specificationId);
            const dataTypesForSpecification = specification
                ? _.get(specDecoratorsBySpecId, specification.id, [])
                : [];

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

            const direction = determineDirection(logicalFlow, parentEntityRef);

            return {
                logicalFlow,
                ratingsByDefId,
                dataTypesForLogicalFlow,
                physicalFlow,
                specification,
                dataTypesForSpecification,
                assessmentRatings,
                direction
            };
        })
        .sortBy(d => d.logicalFlow.target.name, d => d.logicalFlow.source.name)
        .value();
}


export function mkLogicalFromFlowDetails(d) {
    return _.pick(d, ["logicalFlow", "ratingsByDefId", "dataTypesForLogicalFlow", "assessmentRatings", "direction", "visible"]);
}
