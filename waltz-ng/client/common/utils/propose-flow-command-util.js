// waltz-ng/client/common/utils/flow-command-util.js
import _ from "lodash";
import {PROPOSAL_TYPES} from "../constants";

export function buildProposalFlowCommand({physicalFlow,parentEntityRef = null, specification, logicalFlow, dataType, selectedReason, proposalType}) {
    if (!physicalFlow || !specification || !logicalFlow) return null;

    const mkReason = (rating) => ({
        ratingId: rating.id,
        description: rating.description
    });

    const spec = {
        owningEntity: proposalType === PROPOSAL_TYPES.CREATE?parentEntityRef:{ id: physicalFlow.id, kind: physicalFlow.kind },
        name: specification.name,
        description: specification.description,
        format: specification.format,
        lastUpdatedBy: "waltz",
        externalId: !_.isEmpty(specification.externalId) ? specification.externalId : null,
        id: specification.id || null
    };

    const logicalFlowData = {
        logicalFlowId: logicalFlow.id || null,
        source: logicalFlow.source || null,
        target: logicalFlow.target || null
    };

    const flowAttributes = {
        name: physicalFlow.name,
        transport: physicalFlow.transport,
        frequency: physicalFlow.frequency,
        basisOffset: physicalFlow.basisOffset,
        criticality: physicalFlow.criticality,
        description: physicalFlow.description,
        externalId: !_.isEmpty(physicalFlow.externalId) ? physicalFlow.externalId : null
    };

    return {
        specification: spec,
        flowAttributes,
        logicalFlowId: logicalFlowData.logicalFlowId ?? null,
        source: logicalFlowData.source ?? null,
        target: logicalFlowData.target ?? null,
        physicalFlowId: proposalType === PROPOSAL_TYPES.CREATE ? null : physicalFlow.id,
        dataTypeIds: dataType,
        proposalType,
        reason: selectedReason && selectedReason.rating && selectedReason.rating[0]
            ? mkReason(selectedReason.rating[0])
            : null
    };
}