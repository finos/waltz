import {writable} from "svelte/store";

export const store = writable({});
export const processor = writable(() => console.log("no processor defined"));
export const selectedNode = writable(null);
export const selectedFlow = writable(null);
export const selectedAnnotation = writable(null);