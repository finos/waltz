import {writable} from "svelte/store";

export const tabs = {
    OVERVIEW: "overview",
    FILTERS: "filters",
    COLUMNS: "columns",
    PEOPLE: "people"
}

export const showGridSelector = writable(true);
