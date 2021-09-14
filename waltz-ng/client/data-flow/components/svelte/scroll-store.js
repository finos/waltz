import {derived, writable} from "svelte/store";
import _ from "lodash";
import {dimensions} from "./scroll-utils"
import {tweened} from "svelte/motion";
import {scaleBand} from "d3-scale";


export const layoutDirections = {
    categoryToClient: "categoryToClient",
    clientToCategory: "clientToCategory"
}


export const flowDirections = {
    OUTBOUND: "OUTBOUND",
    INBOUND: "INBOUND"
}

export const categories = writable([]);
export const clients = writable([]);
export const arcs = writable([]);
export const clientFilter = writable(() => true);
export const categoryFilter = writable(() => true);
export const layoutDirection = writable(layoutDirections.clientToCategory)
export const highlightClass = writable(null);
export const rainbowTipProportion = tweened(0.2, { duration: 600, delay: 600 });

export const selectedClient = writable(null);
export const focusClient = writable(null);

export const flowDirection = derived(layoutDirection, (direction) => {
    return direction === layoutDirections.categoryToClient ? flowDirections.OUTBOUND : flowDirections.INBOUND
})

export const filteredCategories = derived([categoryFilter, categories], ([q, cats]) => {
    return q == null
        ? cats
        : _.filter(cats, q);
})

export const filteredClients = derived([clientFilter, clients], ([q, cs]) => {
    return q === null
        ? cs
        : _.filter(cs, q)
});

export const filteredArcs = derived([arcs, filteredClients, filteredCategories], ([acs, fcs, fcats]) => {

    clientScrollOffset.set(0);

    const filteredClientIds = _.map(fcs, c => c.id);
    const filteredCatIds = _.map(fcats, c => c.id);

    const filteredArcList = _.filter(acs, a => _.includes(filteredClientIds, a.clientId) && _.includes(filteredCatIds, a.categoryId));

    return filteredArcList;
});


export const clientScale = derived(filteredClients, (c) => scaleBand()
    .padding(0.2)
    .domain(_.map(c, "id"))
    .range([0, _.max([c.length * dimensions.client.height , dimensions.diagram.height])]));

export const categoryScale = derived(filteredCategories, c => scaleBand()
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
        }

        const cliLayout = {
            id: a => a.clientId,
            scale: cliScale,
            dimensions: dimensions.client,
            offset: (x) => x
        }

        if (layoutDir === layoutDirections.categoryToClient) {
            return {
                left: catLayout,
                right: cliLayout,
                clientTranslateX: dimensions.diagram.width - dimensions.client.width,
                categoryTranslateX: 0
            }
        } else if (layoutDir === layoutDirections.clientToCategory) {
            return {
                left: cliLayout,
                right: catLayout,
                clientTranslateX: 0,
                categoryTranslateX: dimensions.diagram.width - dimensions.category.width
            }
        } else {
            throw "layout direction: '" + layoutDir + "' not recognised!!"
        }
    });