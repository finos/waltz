import {writable} from "svelte/store";


export const selectedDiagram = writable(null);
export const selectedInstance = writable(null);
export const callouts = writable([]);
export const hoveredCallout = writable(null);