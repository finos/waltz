import {initialiseData} from "../../../common";

import template from "./physical-flow-section.html";


const bindings = {
    parentEntityRef: '<',
};


const initialState = {};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
    };

    vm.$onChanges = (changes) => {
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzPhysicalFlowSection'
};
