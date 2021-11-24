import {derived, writable} from "svelte/store";
import {groupQuestions, indexResponses} from "../../../survey-utils";

export let surveyDetails = writable(null);
export let questions = writable([]);
export let responses = writable([]);
export let selectedSection = writable(null);

export let groupedQuestions = derived(questions, ($questions) => groupQuestions($questions));
export let responsesByQuestionId = derived(responses, ($responses) => indexResponses($responses));