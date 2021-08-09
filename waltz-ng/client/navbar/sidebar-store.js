import {writable} from "svelte/store";
import {dynamicSections, dynamicSectionsByKind} from "../dynamic-section/dynamic-section-definitions";

const f = _.chain(dynamicSections)
    .map(d => d.name)
    .maxBy(d => d.length)
    .value();
console.log(f);

function createStore() {
    const {subscribe, set} = writable([]);

    return {
        subscribe,
        loadForPageKind: (pageKind) => set(dynamicSectionsByKind[pageKind]),
    };
}

export const availableSections = createStore();
export const sidebarExpanded = writable(true);