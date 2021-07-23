import {writable} from "svelte/store";
import {dynamicSectionsByKind}  from "../dynamic-section/dynamic-section-definitions";


function createStore() {
    const {subscribe, set} = writable([]);

    return {
        subscribe,
        setPageKind: (pageKind) => set(dynamicSectionsByKind[pageKind]),
    };
}

const store = createStore();

export default store;