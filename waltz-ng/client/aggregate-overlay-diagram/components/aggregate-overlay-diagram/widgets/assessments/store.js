import {writable} from "svelte/store";

export const Modes = {
    BAR: Symbol("BAR"),
    BOX: Symbol("BOX")
};

export const renderMode = writable(Modes.BOX);