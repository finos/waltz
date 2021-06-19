import {writable} from "svelte/store";

export const formData = writable({
    name: ""
});

export const recentlyCreated = writable([]);