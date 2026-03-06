import {writable} from "svelte/store";

const TABS = {
    ACTIVE: "Active",
    COMPLETED: "Completed"
}

export const filters = writable({
    [TABS.ACTIVE]: {
        state: [],
        change: [],
        proposer: [],
        action: []
    },
    [TABS.COMPLETED]: {
        state: [],
        change: [],
        proposer: [],
        action: []
    },
})