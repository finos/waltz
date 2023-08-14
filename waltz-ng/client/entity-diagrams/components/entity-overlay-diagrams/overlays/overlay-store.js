import {writable} from "svelte/store";

export const AssessmentRenderModes = {
    BAR: Symbol("BAR"),
    BOX: Symbol("BOX")
};

export const assessmentRenderMode = writable(AssessmentRenderModes.BOX);

export const appChangeAdditionalYears = writable(5);