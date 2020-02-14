import template from "./planned-decommission-info.html";
import {initialiseData} from "../../../common";


const bindings = {
    plannedDecommission: "<",
    replacementApps: "<?"
};


const initialState = {
    replacementApps: [],
};


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzPlannedDecommissionInfo"
};
