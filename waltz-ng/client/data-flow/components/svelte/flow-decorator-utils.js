import _ from "lodash";

const colors = {
    APPLICATION: {
        fill: "#eef8ff",
        stroke: "#6fbdff"
    },
    ACTOR: {
        fill: "#f0e9ff",
        stroke: "#9f75fd"
    },
    END_USER_APPLICATION: {
        fill: "#fff0e9",
        stroke: "#fd9575"
    }
};

export function getNodeColors(kind) {
    const c = colors[kind];

    return c || { fill: "#ccc", stroke: "#999" };
}


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
};


const cliLayout = {
    id: a => a.clientId,
    scale: (catYPos, cliYPos) => cliYPos,
    dimensions: dimensions.client,
    offset: (x) => x
};


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
        .uniqBy(d => d.id)
        .value()
}


export function mkCategories(summarisedFlows){
    return _
        .chain(summarisedFlows)
        .groupBy(d => d.category.id)
        .reduce((acc, xs, k) => {
            const category = {
                id: xs[0].category.id,
                name: xs[0].category.name,
                category: xs[0].category,
                hasChildren: _.some(xs, x => x.hasChildren),
                searchableDataTypeNames: _
                    .chain(xs)
                    .flatMap(x => x.searchableDataTypeNames)
                    .uniq()
                    .join(" ")
                    .value()
            };

            return [...acc, category];
        }, [])
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


export function summariseFlows(flowInfo, noOpinionRating) {
    return _
        .chain(flowInfo)
        .map(d => Object.assign({}, d, {key: `cat_${d.rollupDataType.id}_cli_${d.counterpart.id}`}))
        .groupBy(d => d.key)
        .mapValues((decoratorsForKey, key) => {
            const flow = _.head(decoratorsForKey);

            const ratingCounts = _
                .chain(decoratorsForKey)
                .filter(decorator => decorator.actualDataType.id !== decorator.rollupDataType.id)
                .countBy(decorator => decorator.classificationId)
                .value();

            const exactFlow = _.find(
                decoratorsForKey,
                decorator => decorator.actualDataType.id === decorator.rollupDataType.id);

            const lineRating = _.get(exactFlow, ["classificationId"], noOpinionRating?.id); //TODO: make this the no opinion id

            const lineLifecycleStatus = _.get(exactFlow, ["flowEntityLifecycleStatus"], "ACTIVE");

            const actualDataTypeIds = _
                .chain(decoratorsForKey)
                .map(decorator => decorator.actualDataType?.id)
                .uniq()
                .value();

            const searchableDataTypeNames = _
                .chain(decoratorsForKey)
                .map(decorator => decorator.actualDataType?.name)
                .uniq()
                .concat([flow.rollupDataType?.name])
                .value();

            const rollupDataTypeIds = _
                .chain(decoratorsForKey)
                .map(decorators => decorators.rollupDataType?.id)
                .uniq()
                .value();

            return {
                key,
                ratings: decoratorsForKey,
                hasChildren: !_.isEmpty(ratingCounts),
                ratingCounts,
                lineRating,
                lineLifecycleStatus,
                actualDataTypeIds,
                searchableDataTypeNames,
                rollupDataTypeIds,
                flowId: flow.flowId,
                category: flow.rollupDataType,
                client: flow.counterpart
            };
        })
        .value();
}
