import {writable} from "svelte/store";

export const Modes = {
    LIST: "LIST",
    EDIT: "EDIT",
    DETAIL: "DETAIL"
}

export const selectedAuthSource = writable(null);

export const mode = writable(Modes.LIST);