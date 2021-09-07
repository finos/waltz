import _ from "lodash";
import {randomPick} from "./scroll-utils";

export function mkClientsOld(){
    return _
        .range(0, 80)
        .map(d => ({
            name: `C${d}`,
            id: d
        }))
}

export function mkClients(flowInfo){
    return _
        .chain(flowInfo)
        .map(d => ({
            name: d.counterpart.name,
            id: d.counterpart.id
        }))
        .uniq()
        .value()
}

export function mkCategoriesOld(){
    return _
        .range(1, 15)
        .map(d => ({
            name: `Category ${d}`,
            id: d,
            hasChildren: d % 3 === 0
        }));
}

export function mkCategories(flowInfo){
    console.log({flowInfo});
    return _
        .chain(flowInfo)
        .map(d => ({
            name: d.rollupDataType.name,
            id: d.rollupDataType.id,
            hasChildren: false
        }))
        .uniq()
        .value();
}


export function mkArcsOld(clients, categories){

    let id = 0;

    return _
        .chain(clients)
        .flatMap(d => _.map(
            _.range(Math.ceil(Math.random() * 5)),
            () => ({
                id: id++,
                clientId: d.id,
                categoryId: randomPick(_.map(categories, d => d.id)),
                ratingId: Math.ceil(Math.random() * 6), // overall flow rating
                tipRatings: mkTipRatings()
            })))
        .uniqBy(d => d.clientId + "_" + d.categoryId)
        .value();
}

export function mkArcs(summarisedFlows){

    return _
        .chain(summarisedFlows)
        .map(d => ({
            id: d.key,
            clientId: d.clientId,
            categoryId: d.categoryId,
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