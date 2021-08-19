import template from "./companion-app-rules-section.html"
import {initialiseData} from "../../../common";

import CompanionAppRulesPanel from "../svelte/CompanionAppRulesPanel.svelte";

const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    CompanionAppRulesPanel
};

function controller() {

    const vm = initialiseData(this, initialState);

}


const component ={
    template,
    bindings,
    controller
}

export default {
    id: "waltzCompanionAppRulesSection",
    component
}