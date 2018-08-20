import { initialiseData } from "../../../common";

import template from "./assessment-rating-sub-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAssessmentRatingSubSection"
};
