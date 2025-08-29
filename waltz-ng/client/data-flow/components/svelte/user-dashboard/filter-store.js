import {writable} from "svelte/store";

export const filters = writable({
    state: [],
    change: [],
    proposer: []
});

export const tempFilters = writable({
    state: [],
    change: [],
    proposer: []
});