import {writable} from "svelte/store";

export const Modes = {
    TABLE: "TABLE",
    TREE: "TREE",
    EDIT: "EDIT",
    DETAIL: "DETAIL"
}

export const selectedEnvironment = writable(null);

export const mode = writable(Modes.TABLE);