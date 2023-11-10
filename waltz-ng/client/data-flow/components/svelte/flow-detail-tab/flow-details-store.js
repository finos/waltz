import {writable, get} from "svelte/store";
import _ from "lodash";

export let filters = writable([]);
export let selectedLogicalFlow = writable(null);
export let selectedPhysicalFlow = writable(null);


export function updateFilters(id, newFilter) {
    return filters.update(filtersList => {
        const withoutFilter = _.reject(filtersList, d => d.id === id);
        return _.concat(withoutFilter, newFilter);
    })
}