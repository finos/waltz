import {writable} from "svelte/store";

export const filters = writable({
    "Actionable": {
        state: [],
        change: [],
        proposer: []
    },
    "Historical": {
        state: [],
        change: [],
        proposer: []
    },
})