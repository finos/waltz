import {derived, writable} from "svelte/store";
import _ from "lodash";
import {prepareData} from "./data-type-decorator-view-grid-utils";

export const selectedDecorator = writable(null);
export const selectedDataType = writable(null);
export const viewData = writable(null);
export const filters = writable([]);


export function updateFilters(filters, id, newFilter) {
    return filters.update(filtersList => {
        const withoutFilter = _.reject(filtersList, d => d.id === id);
        return _.concat(withoutFilter, newFilter);
    })
}

export const enrichedDecorators = derived([viewData], ([$viewData]) => {
    if ($viewData) {
        return prepareData($viewData);
    } else {
        return [];
    }
});