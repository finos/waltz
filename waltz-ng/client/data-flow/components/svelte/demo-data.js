import _ from "lodash";
import {selectedClient} from "./scroll-store";


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
            ratingId: d.lineRating, // overall flow rating
            tipRatings: mkTipRatings(d.ratingCounts)
        }))
        .value();
}


function mkTipRatingsOld() {
    if (Math.random() > 0.5) {
        return null;
    } else {
        return [
            {ratingId: 0, count: Math.floor(Math.random() * 4)},
            {ratingId: 1, count: Math.floor(Math.random() * 8)},
            {ratingId: 2, count: Math.floor(Math.random() * 16)},
            {ratingId: 3, count: Math.floor(Math.random() * 4)}
        ]
    }
}
    
    
function mkTipRatings(ratingCounts){
    return _.map(ratingCounts, (v, k) => ({ ratingId: k, count: v }));
}