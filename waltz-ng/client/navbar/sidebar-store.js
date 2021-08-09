import {writable} from "svelte/store";
import {dynamicSectionsByKind} from "../dynamic-section/dynamic-section-definitions";

export const sidebarExpanded = writable(true);
export const sidebarVisible = writable(false);

function createStore() {
    const {subscribe, set} = writable([]);

    return {
        subscribe,
        loadForPageKind: (pageKind) => {
            const sections = dynamicSectionsByKind[pageKind] || [];
            sidebarVisible.set(! _.isEmpty(sections));
            console.log({sections})
            return set(sections);
        },
    };
}

export const availableSections = createStore();
