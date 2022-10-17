import {writable} from "svelte/store";


export const UploadModes = {
    ADD_ONLY: "ADD_ONLY",
    REPLACE: "REPLACE"
}

export const rawInvolvements = writable("");
export const selectedKind = writable(null);
export const resolvedRows = writable([]);
export const resolutionErrors = writable([]);
export const involvements = writable([]);
export const uploadMode = writable(UploadModes.ADD_ONLY);