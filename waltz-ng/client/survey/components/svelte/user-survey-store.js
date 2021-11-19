import {writable} from "svelte/store";

export const selectSurveyRow = writable(() => console.log("Selecting a row in a survey table"));

export const selectedSurveyStatusCell = writable(null);