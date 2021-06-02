import {writable} from "svelte/store";

export const store = writable({});
export const processor = writable(() => console.log("no processor defined"));