import template from "./scenario-list.html";
import {initialiseData} from "../../../common";
import _ from "lodash";


const bindings = {
    scenarios: "<",
    onAddScenario: "<",
    onCloneScenario: "<",
    onSelectScenario: "<",
    onConfigureScenario: "<"
};


const initialState = {
    scenarios: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
    };

}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzScenarioList",
    component
};