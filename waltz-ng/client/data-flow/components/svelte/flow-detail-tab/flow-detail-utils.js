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


function groupRatingsByDefId(assessmentRatingsForLogicalFlow, ratingSchemeItemsById) {
    return _
        .chain(assessmentRatingsForLogicalFlow)
        .groupBy(r => r.assessmentDefinitionId)
        .mapValues(v => _
            .chain(v)
            .map(r => ratingSchemeItemsById[r.ratingId])
            .filter(d => d != null)
            .sortBy(r => r.position, r => r.name)
            .value())
        .value();
}


export function mkFlowDetails(flowView, parentEntityRef) {

    const logicalDecoratorsByFlowId = _.groupBy(flowView.logicalFlowDataTypeDecorators, d => d.dataFlowId);
    const specDecoratorsBySpecId = _.groupBy(flowView.physicalSpecificationDataTypeDecorators, d => d.dataFlowId);
    const logicalFlowRatingsByFlowId = _.groupBy(flowView.logicalFlowRatings, d => d.entityReference.id);
    const physicalFlowRatingsByFlowId = _.groupBy(flowView.physicalFlowRatings, d => d.entityReference.id);
    const physicalSpecRatingsByFlowId = _.groupBy(flowView.physicalSpecificationRatings, d => d.entityReference.id);
    const ratingSchemeItemsById = _.keyBy(flowView.ratingSchemeItems, d => d.id);
    const specsById = _.keyBy(flowView.physicalSpecifications, d => d.id);
    const physicalFlowsByLogicalFlowId = _.groupBy(flowView.physicalFlows, d => d.logicalFlowId);

    return _
        .chain(flowView.logicalFlows)
        .flatMap(d => {
            const physicalFlows = _.get(physicalFlowsByLogicalFlowId, d.id, []);
            return _.isEmpty(physicalFlows)
                ? [{logicalFlow: d, physicalFlow: null}]
                : _.map(physicalFlows, p => ({logicalFlow: d, physicalFlow: p}))
        })
        .map(t => {
            const logicalFlow = t.logicalFlow;
            const physicalFlow = t.physicalFlow;

            const assessmentRatingsForLogicalFlow = _.get(logicalFlowRatingsByFlowId, logicalFlow.id, []);
            const assessmentRatingsForPhysicalFlow = _.get(physicalFlowRatingsByFlowId, physicalFlow?.id, []);
            const assessmentRatingsForPhysicalSpec = _.get(physicalSpecRatingsByFlowId, physicalFlow?.specificationId, []);
            const dataTypesForLogicalFlow = _.get(logicalDecoratorsByFlowId, logicalFlow.id, []);
            const specification = _.get(specsById, physicalFlow?.specificationId);
            const dataTypesForSpecification = specification
                ? _.get(specDecoratorsBySpecId, specification.id, [])
                : [];

            const logicalFlowRatingsByDefId = groupRatingsByDefId(assessmentRatingsForLogicalFlow, ratingSchemeItemsById);
            const physicalFlowRatingsByDefId = groupRatingsByDefId(assessmentRatingsForPhysicalFlow, ratingSchemeItemsById);
            const physicalSpecRatingsByDefId = groupRatingsByDefId(assessmentRatingsForPhysicalSpec, ratingSchemeItemsById);

            const direction = determineDirection(logicalFlow, parentEntityRef);

            return {
                direction,
                logicalFlow,
                physicalFlow,
                specification,
                dataTypesForLogicalFlow,
                dataTypesForSpecification,
                logicalFlowRatingsByDefId,
                physicalFlowRatingsByDefId,
                physicalSpecRatingsByDefId,
                allRatings: _.concat(
                    assessmentRatingsForLogicalFlow,
                    assessmentRatingsForPhysicalFlow,
                    assessmentRatingsForPhysicalSpec)
            };
        })
        .sortBy([
            d => d.logicalFlow.target.name,
            d => d.logicalFlow.source.name
        ])
        .value();
}
