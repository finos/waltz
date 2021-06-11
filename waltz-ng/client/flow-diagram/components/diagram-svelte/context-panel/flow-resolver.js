import _ from "lodash";
import {sameRef} from "../../../../common/entity-utils";
import {toGraphFlow, toGraphId, toGraphNode} from "../../../flow-diagram-utils";

/**
 * adds/removes logical flows
 */
export function prepareUpdateCommands(flows = [],
                                      existingEntities = [],
                                      isUpstream,
                                      baseEntity) {

    const isAddition = f => ! f.existing && f.used;
    const isRemoval = f =>  f.existing && ! f.used

    const newFlows = _
        .chain(flows)
        .filter(isAddition)
        .reject(f => _.some(existingEntities, ent => sameRef(ent, f.counterpartEntity)))
        .value()

    const nodeAdditions = _.map(newFlows, f => toGraphNode(f.counterpartEntity));
    const flowAdditions = _.map(newFlows, f => toGraphFlow(f.logicalFlow));

    const moves = _.map(
        newFlows,
        f => ({
            id: toGraphId(f.counterpartEntity),
            refId: toGraphId(baseEntity),
            dx: _.random(-80, 80),
            dy: _.random(50, 80) * (isUpstream ? -1 : 1)
        }));

    const flowRemovals = _
        .chain(flows)
        .filter(isRemoval)
        .map(f => ({
            id: toGraphId(f.logicalFlow),
            source: toGraphId(f.logicalFlow.source),
            target: toGraphId(f.logicalFlow.target)
        }))
        .value();

    return {
        nodeAdditions,
        flowAdditions,
        flowRemovals,
        moves
    };
}



export function mkFlows(logicalFlows = [], node, isUpstream, existingEntities = []) {
    const counterpartPropName = isUpstream
        ? "source"
        : "target";

    const selfPropName = isUpstream
        ? "target"
        : "source";

    return _
        .chain(logicalFlows)
        .filter(f => f[selfPropName].id === node.id)
        .reject(f => f[counterpartPropName].id === node.id)
        .map(f => Object.assign({}, f, { kind: "LOGICAL_DATA_FLOW" }))
        .map(f => {
            const counterpartEntity = f[counterpartPropName];
            const flowExists = _.some(existingEntities, ref => sameRef(ref, counterpartEntity)); // this needs to be changed to existing flows
            return {
                counterpartEntity,
                logicalFlow: f,
                used: flowExists,
                existing: flowExists
            };
        })
        .sortBy(d => d.counterpartEntity.name.toLowerCase())
        .value();
}


/**
 * adds/removes physical flows
 */
export function preparePhysicalFlowUpdates(flows) {

    const additions = _
        .chain(flows)
        .filter(f => ! f.existing && f.used)
        .map(f => ({
            ref: {
                id: f.physicalFlow.logicalFlowId,
                kind: "LOGICAL_DATA_FLOW"
            },
            decoration: {
                id: f.physicalFlow.id,
                kind: "PHYSICAL_FLOW"
            }
        }))
        .value();

    const removals = _
        .chain(flows)
        .filter(f => f.existing && ! f.used)
        .map(f => ({
            ref: {
                id: f.physicalFlow.logicalFlowId,
                kind: "LOGICAL_DATA_FLOW"
            },
            decoration: {
                id: f.physicalFlow.id,
                kind: "PHYSICAL_FLOW"
            }
        }))
        .value();

    return { additions, removals };
}


export function preparePhysicalFlows(
    physicalFlows = [],
    physicalSpecifications = [],
    existingEntities = [])
{
    const specsById = _.keyBy(physicalSpecifications, "id");
    return _.chain(physicalFlows)
        .map(f => {
            const currentlyUsed = _.some(existingEntities, existing => sameRef(existing, { kind: "PHYSICAL_FLOW", id: f.id }))
            return {
                used: currentlyUsed,
                existing: currentlyUsed,
                physicalFlow: f,
                specification: specsById[f.specificationId]
            };
        })
        .filter(f => !_.isNil(f.specification))
        .sortBy(d => d.specification.name.toLowerCase())
        .value();
}
