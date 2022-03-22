import {writable, derived} from "svelte/store";
import {selectedGrid} from "./report-grid-store";
import _ from "lodash";
import { mkLocalStorageFilterKey } from "./report-grid-utils";


function createStore() {
    const {subscribe, set, update} = writable([]);

    const add = (colRef) => {
        update((all) => _.concat(all, [colRef]));
    };

    const remove = (colRef) => {
        update((all) => all.filter((t) => t !== colRef));
    };


    return {
        subscribe,
        set,
        add,
        remove,
    }
}

export const activeSummaries = createStore();


const saveSummariesToLocalStorage = derived([selectedGrid, activeSummaries], ([$selectedGrid, $activeSummaries]) => {

    if (!$selectedGrid){
        return;
    }

    const key = mkLocalStorageFilterKey($selectedGrid?.definition.id);
    localStorage.setItem(key, JSON.stringify($activeSummaries));
})    
.subscribe(() => {});