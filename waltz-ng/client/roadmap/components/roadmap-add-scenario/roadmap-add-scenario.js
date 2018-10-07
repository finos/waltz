import template from "./roadmap-add-scenario.html";
import {initialiseData} from "../../../common";

const bindings = {
    roadmap: "<",
    onCancel: "<"
};


const initialState = {};


function controller() {
    const vm = initialiseData(this, initialState);
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