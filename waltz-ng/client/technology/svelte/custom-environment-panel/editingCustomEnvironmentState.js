import {writable} from "svelte/store";

export const EnvironmentModes = {
    TABLE: "TABLE",
    TREE: "TREE",
    EDIT: "EDIT",
    DETAIL: "DETAIL"
}

export const PanelModes = {
    VIEW: "VIEW",
    REGISTER: "REGISTER"
}

export const panelMode = writable(PanelModes.VIEW);