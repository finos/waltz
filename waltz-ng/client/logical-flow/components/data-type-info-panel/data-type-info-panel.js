import template from "./data-type-info-panel.html"
import {initialiseData} from "../../../common";
import DataTypeInfoPanel from "./DataTypeInfoPanel.svelte";

const bindings = {
    datatype: "<",
    parentEntityRef: "<",
    logicalFlow: "<"
};


const initialState = {
    DataTypeInfoPanel
};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

}


controller.$inject = ["ServiceBroker"];


const component = {
    template,
    controller,
    bindings
}

export default {
    id: "waltzDataTypeInfoPanel",
    component
}