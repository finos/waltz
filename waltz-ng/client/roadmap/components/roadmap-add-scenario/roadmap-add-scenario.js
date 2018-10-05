import template from "./roadmap-add-scenario.html";
import {initialiseData} from "../../../common";

const bindings = {
    roadmap: "<",
    onCancel: "<"
};


const initialState = {};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);
    global.vm = vm;
}


controller.$inject = [];


const component = {
    controller,
    template,
    bindings
};


export default {
    id: "waltzRoadmapAddScenario",
    component
};