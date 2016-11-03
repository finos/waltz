import {initialiseData} from "../../../common";

const bindings = {
    sourceEntity: '<',
    specification: '<',
    targetEntity: '<',
    flowAttributes: '<',
    onSpecificationFocus: '<',
    onFlowAttributesFocus: '<',
    onTargetFocus: '<'
};


const template = require('./physical-flow-edit-overview.html');


const initialState = {
    onSpecificationFocus: () => console.log("No onSpecificationFocus handler defined for physical-flow-edit-overview"),
    onFlowAttributesFocus: () => console.log("No onFlowAttributesFocus handler defined for physical-flow-edit-overview"),
    onTargetFocus: () => console.log("No onTargetFocus handler defined for physical-flow-edit-overview")
};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;