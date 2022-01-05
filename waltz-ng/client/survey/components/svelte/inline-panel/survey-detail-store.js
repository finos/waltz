import {derived, writable} from "svelte/store";
import {groupQuestions, indexResponses} from "../../../survey-utils";

export let surveyDetails = writable(null);
export let formDetails = writable(null);
export let responses = writable([]);
export let selectedSection = writable(null);

export let questions = derived(formDetails, ($formDetails) => $formDetails?.activeQuestions);
export let missingMandatoryQuestionIds = derived(formDetails, ($formDetails) => $formDetails?.missingMandatoryQuestionIds)
export let groupedQuestions = derived(questions, ($questions) => groupQuestions($questions));
export let responsesByQuestionId = derived(responses, ($responses) => indexResponses($responses));