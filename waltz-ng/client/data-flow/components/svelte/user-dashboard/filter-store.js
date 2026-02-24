import {writable} from "svelte/store";

export const filters = writable({
    "Actionable": {
        state: [],
        change: [],
        proposer: [],
        action: []
    },
    "Historical": {
        state: [],
        change: [],
        proposer: [],
        action: []
    },
})