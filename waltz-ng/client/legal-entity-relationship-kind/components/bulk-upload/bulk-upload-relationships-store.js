import {derived, writable} from "svelte/store";
import _ from "lodash";

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
export const saveResponse = writable(null);
export const activeMode = writable(Modes.INPUT);
export const uploadMode = writable(UploadModes.ADD_ONLY);
export const inputString = writable(null);


export const sortedHeaders = derived([resolveResponse], ([$resolveResponse]) => {
    return _.isNull($resolveResponse)
        ? []
        : _.sortBy($resolveResponse.assessmentHeaders, d => d.columnId);
});

export const resolvedRows = derived([resolveResponse], ([$resolveResponse]) => {
    return _.isNull($resolveResponse)
        ? []
        : _.sortBy($resolveResponse.rows, d => d.rowNumber);
});

export const anyErrors = derived([resolveResponse], ([$resolveResponse]) => {
    return _.isNull($resolveResponse)
        ? false
        : _.some($resolveResponse.rows, d => d.legalEntityRelationship.operation === "ERROR");
});
