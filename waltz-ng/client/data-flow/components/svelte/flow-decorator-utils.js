export const dimensions = {
    client: {
        height: 25,
        width: 250,
        iconPadding: 30
    },
    category: {
        height: 40,
        width: 250,
        iconPadding: 0
    },
    clientList: {
        paddingTop: 20,
        innerPadding: 1.4
    },
    diagram: {
        height: 750,
        width: 900
    }
};

const catLayout = {
    id: a => a.categoryId,
    scale: (catYPos, cliYPos) => catYPos,
    dimensions: dimensions.category,
    offset: () => 0
}

const cliLayout = {
    id: a => a.clientId,
    scale: (catYPos, cliYPos) => cliYPos,
    dimensions: dimensions.client,
    offset: (x) => x
}

export const layout = {
    categoryToClient: {
        left: catLayout,
        right: cliLayout,
        clientTranslateX: dimensions.diagram.width - dimensions.client.width,
        categoryTranslateX: 0
    },
    clientToCategory: {
        left: cliLayout,
        right: catLayout,
        clientTranslateX: 0,
        categoryTranslateX: dimensions.diagram.width - dimensions.category.width
    }
}


export const activeLayout = layout.clientToCategory;


export function randomPick(xs) {
    if (!xs) throw new Error("Cannot pick from a null set of options");

    const choiceCount = xs.length - 1;
    const idx = Math.round(Math.random() * choiceCount);
    return xs[idx];
}


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