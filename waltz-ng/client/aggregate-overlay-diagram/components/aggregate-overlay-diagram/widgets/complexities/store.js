import {writable} from "svelte/store";


export const ComplexityModes = {
    TOTAL: Symbol("TOTAL"),
    AVERAGE: Symbol("AVERAGE")
};

export const complexityRenderMode = writable(ComplexityModes.TOTAL);