import _ from "lodash";


export function mkClients(summarisedFlows, physicalFlows = []){
    return _
        .chain(summarisedFlows)
        .map(d => {

            const flowIds = _
                .chain(summarisedFlows) //summarised flows are keyed by dt and counterpart app
                .filter(f => f.client.id === d.client.id)
                .map(f => f.flowId)
                .value();

            const physFlows = _
                .chain(physicalFlows)
                .filter(f => _.includes(flowIds, f.logicalFlowId))
                .filter(f => !f.isRemoved)
                .value();

            return {
                name: d.client.name,
                id: d.client.id,
                kind: d.client.kind,
                physicalFlows: physFlows,
            }
        })
        .uniq()
        .value()
}

export function mkCategories(summarisedFlows){
    return _
        .chain(summarisedFlows)
        .map(d => ({
            name: d.category.name,
            id: d.category.id,
            hasChildren: d.hasChildren
        }))
        .uniq()
        .value();
}

export function mkArcs(summarisedFlows){
    return _
        .chain(summarisedFlows)
        .map(d => ({
            id: d.key,
            clientId: d.client.id,
            categoryId: d.category.id,
            flowId: d.flowId,
            ratingId: d.lineRating, // overall flow rating
            lifecycleStatus: d.lineLifecycleStatus,
            tipRatings: mkTipRatings(d.ratingCounts),
            actualDataTypeIds: d.actualDataTypeIds,
            rollupDataTypeIds: d.rollupDataTypeIds

        }))
        .value();
}
    
function mkTipRatings(ratingCounts){
    return _.map(ratingCounts, (v, k) => ({ ratingId: k, count: v }));
}