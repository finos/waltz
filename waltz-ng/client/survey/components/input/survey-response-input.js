import template from "./survey-response-input.html"
import {initialiseData} from "../../../common";


const bindings = {
    question: "<",
    response: "<",
    options: "<",
    instanceId: "<",
    saveComment: "<",
    saveResponse: "<",
    saveDateResponse: "<",
    saveEntityResponse: "<",
    saveEntityListResponse: "<"
};
const initialState = {};


function controller() {
    const vm = initialiseData(this, initialState);

    controller.$onChanges = () => console.log("oc", vm)
}

controller.$inject = [];


const component = {
    template, bindings, controller
};

export default {
    id: "waltzSurveyResponseInput",
    component
};