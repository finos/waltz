import template from "./survey-response-input.html"
import {initialiseData} from "../../../common";
import ARCSurveyComponent from "../svelte/arc-survey-components/ARCSurveyComponent.svelte";

const bindings = {
    question: "<",
    response: "<",
    options: "<",
    instanceId: "<",
    saveComment: "<",
    saveStringResponse: "<",
    saveNumberResponse: "<",
    saveBooleanResponse: "<",
    saveDateResponse: "<",
    saveEntityResponse: "<",
    saveEntityListResponse: "<",
    saveListResponse: "<",
    subject: "<",
    surveyCustomFieldTypes: "<"
};

const initialState = {
    ARCSurveyComponent,
};


function controller() {
    const vm = initialiseData(this, initialState);
}

controller.$inject = [];


const component = {
    template, bindings, controller
};

export default {
    id: "waltzSurveyResponseInput",
    component
};