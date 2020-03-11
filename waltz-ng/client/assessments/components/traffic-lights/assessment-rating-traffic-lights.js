import {initialiseData} from "../../../common";

import template from "./assessment-rating-traffic-lights.html";


const bindings = {
    assessments: "<",
};


const initialState = {
    assessments: [],
};


function controller(){
    const vm = initialiseData(this, initialState);
}


controller.$inject = [
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingTrafficLights"
};
