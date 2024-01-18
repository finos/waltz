import {writable} from "svelte/store"

export const selectedMeasurable = writable(null);
export const showPrimaryOnly = writable(false);