import {writable} from "svelte/store";

export const Modes = {
    INPUT: "INPUT",
    LOADING: "LOADING",
    RESOLVED: "RESOLVED"
}

export const UploadModes = {
    ADD_ONLY: "ADD_ONLY",
    REPLACE: "REPLACE"
}


export const resolvedRows = writable([]);
export const activeMode = writable(Modes.INPUT);
export const uploadMode = writable(UploadModes.ADD_ONLY);
export const inputString = writable(null);