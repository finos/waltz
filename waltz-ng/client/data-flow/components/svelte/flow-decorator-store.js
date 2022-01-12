import {derived, writable} from "svelte/store";
import _ from "lodash";
import {dimensions} from "./flow-decorator-utils"
import {tweened} from "svelte/motion";
import {scaleBand} from "d3-scale";
import {termSearch} from "../../../common";
import {containsAll} from "../../../common/list-utils";


export const layoutDirections = {
    categoryToClient: "categoryToClient",
    clientToCategory: "clientToCategory"
};


export const flowDirections = {
    OUTBOUND: "OUTBOUND",
    INBOUND: "INBOUND"
};


export const Modes = {
    DEFAULT: "DEFAULT",
    DECORATOR: "DECORATOR",
    FLOW_SUMMARY: "FLOW_SUMMARY",
    FLOW_DETAIL: "FLOW_DETAIL"
};

export const parentCategory = writable(null);
export const startingCategory = writable(null);

export const categories = writable([]);
export const clients = writable([]);
export const arcs = writable([]);
export const clientQuery = writable(null);
export const categoryQuery = writable(null);
export const entityKindFilter = writable(() => true);
export const assessmentRatingFilter = writable(() => true);
export const layoutDirection = writable(layoutDirections.clientToCategory)
export const highlightClass = writable(null);
export const rainbowTipProportion = tweened(0.2, { duration: 600, delay: 600 });
export const contextPanelMode = writable(Modes.DEFAULT);

export const selectedCategory = writable(null);
export const selectedDecorator = writable(null);
export const selectedClient = writable(null);
export const selectedFlow = writable(null);
export const focusClient = writable(null);


export const flowDirection = derived(layoutDirection, (direction) => {
    return direction === layoutDirections.categoryToClient ? flowDirections.OUTBOUND : flowDirections.INBOUND
});


export const filteredCategories = derived([categoryQuery, categories], ([catQry, cats]) => {
    const filteredCats = _.isEmpty(catQry)
        ? cats
        : termSearch(cats, catQry, ["searchableDataTypeNames"]);

    return _.orderBy(filteredCats, d => _.upperCase(d.name))
});


export const filteredClients = derived([clientQuery, entityKindFilter, assessmentRatingFilter, clients], ([clientQry, entityKindFilter, assessmentRatingFilter, cs]) => {

    const filtered = _.isEmpty(clientQry)
        ? cs
        : termSearch(cs, clientQry, ["name"]);

    return entityKindFilter === null
        ? filtered
        : _.chain(filtered)
            .filter(entityKindFilter)
            .filter(assessmentRatingFilter)
            .orderBy(c => _.upperCase(c.name))
            .value();
});


export const filteredArcs = derived([arcs, filteredClients, filteredCategories], ([acs, fcs, fcats]) => {

    clientScrollOffset.set(0);

    const filteredClientIds = _.map(fcs, c => c.id);
    const filteredCatIds = _.map(fcats, c => c.id);

    return _.filter(acs, a => _.includes(filteredClientIds, a.clientId) && _.includes(filteredCatIds, a.categoryId));
});


export const filterApplied = derived([clients, filteredClients, categories, filteredCategories], ([cs, fcs, cats, fcats]) => {
    return !containsAll(fcs, cs) || !containsAll(fcats, cats);
});


export const displayedClients = derived([filteredArcs, filteredClients], ([$filteredArcs, $filteredClients]) => {
    const clientsInArcs = _.map($filteredArcs, d => d.clientId);
    return _.filter($filteredClients, c => _.includes(clientsInArcs, c.id));
})


export const displayedCategories = derived([filteredArcs, filteredCategories], ([$filteredArcs, $filteredCategories]) => {
    const categoriesInArcs = _.map($filteredArcs, d => d.categoryId);
    return _.filter($filteredCategories, c => _.includes(categoriesInArcs, c.id));
})


export const clientScale = derived(displayedClients, (c) => scaleBand()
    .padding(0.2)
    .domain(_.map(c, "id"))
    .range([0, _.max([(c.length - 1) * (dimensions.client.height * 1.2), dimensions.diagram.height])]));


export const categoryScale = derived(displayedCategories, c => scaleBand()
    .padding(0.2)
    .range([0, dimensions.diagram.height])
    .domain(_.map(c, "id")));


export const clientScrollOffset = tweened(0, {duration: 200});


export const layout = derived(
    [layoutDirection, clientScale, categoryScale],
    ([layoutDir, cliScale, catScale]) => {
        const catLayout = {
            id: a => a.categoryId,
            scale: catScale,
            dimensions: dimensions.category,
            offset: () => 0
        };

        const cliLayout = {
            id: a => a.clientId,
            scale: cliScale,
            dimensions: dimensions.client,
            offset: (x) => x
        };

        if (layoutDir === layoutDirections.categoryToClient) {
            return {
                left: catLayout,
                right: cliLayout,
                clientTranslateX: dimensions.diagram.width - dimensions.client.width,
                categoryTranslateX: 0
            };
        } else if (layoutDir === layoutDirections.clientToCategory) {
            return {
                left: cliLayout,
                right: catLayout,
                clientTranslateX: 0,
                categoryTranslateX: dimensions.diagram.width - dimensions.category.width
            };
        } else {
            throw `layout direction: '${layoutDir}' not recognised`;
        }
    });


export const selectedClientArcs = derived([filteredArcs, selectedClient], ([$filteredArcs, $selectedClient]) => {
    if($selectedClient == null){
        return [];
    } else{
        return _
            .chain($filteredArcs)
            .filter(a => a.clientId === $selectedClient.id)
            .value();
    }
});


export function clearSelections() {
    contextPanelMode.set(Modes.DEFAULT);
    selectedClient.set(null);
    selectedDecorator.set(null);
    selectedFlow.set(null);
    selectedCategory.set(null);
}