import {writable} from "svelte/store";

export const Modes = {
    INPUT: "INPUT",
    LOADING: "LOADING",
    RESOLVED: "RESOLVED",
    REPORT: "REPORT"
}

export const UploadModes = {
    ADD_ONLY: "ADD_ONLY",
    REPLACE: "REPLACE"
}


export const resolveResponse = writable(null);
export const resolvedRows = writable([]);
export const sortedHeaders = writable([]);
export const activeMode = writable(Modes.INPUT);
export const uploadMode = writable(UploadModes.ADD_ONLY);
export const inputString = writable(null);