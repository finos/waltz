import template from "./survey-response-answer.html"
import {initialiseData} from "../../../common";


const bindings = {
    question: "<",
    answer: "<",
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
    id: "waltzSurveyResponseAnswer",
    component
};