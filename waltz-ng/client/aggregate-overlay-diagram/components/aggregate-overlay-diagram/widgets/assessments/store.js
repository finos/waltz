import {writable} from "svelte/store";

export const Modes = {
    BAR: Symbol("BAR"),
    BOX: Symbol("BOX")
};

export const assessmentRenderMode = writable(Modes.BOX);