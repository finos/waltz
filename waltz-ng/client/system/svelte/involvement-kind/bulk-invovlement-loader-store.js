import {writable} from "svelte/store";

export const rawInvolvements = writable("");
export const selectedKind = writable(null);
export const resolvedRows = writable([]);
export const resolveParams = writable(null);