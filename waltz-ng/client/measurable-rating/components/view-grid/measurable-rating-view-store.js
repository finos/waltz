import {writable} from "svelte/store"

export const selectedCategory = writable(null);
export const selectedMeasurable = writable(null);
export const showPrimaryOnly = writable(false);
export const showUnmapped = writable(false);