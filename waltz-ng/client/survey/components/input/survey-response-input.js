import template from "./survey-response-input.html"
import {initialiseData} from "../../../common";


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
    saveListResponse: "<"
};

const initialState = {};


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