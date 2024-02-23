import {writable} from "svelte/store";
import _ from "lodash";

export const selectedDecorator = writable(null);
export const selectedDataType = writable(null);
export const viewData = writable(null);
export const enrichedDecorators = writable([]);
export const filters = writable([]);


export function updateFilters(filters, id, newFilter) {
    return filters.update(filtersList => {
        const withoutFilter = _.reject(filtersList, d => d.id === id);
        return _.concat(withoutFilter, newFilter);
    })
}