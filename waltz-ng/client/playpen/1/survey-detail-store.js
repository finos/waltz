import {writable} from "svelte/store";

export let surveyDetails = writable(null);
export let questions = writable([]);
export let responses = writable([]);
export let selectedSection = writable(null);