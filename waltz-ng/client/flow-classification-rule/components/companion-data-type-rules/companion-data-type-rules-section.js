import template from "./companion-data-type-rules-section.html"
import {initialiseData} from "../../../common";

import CompanionDataTypeRulesPanel from "../svelte/CompanionDataTypeRulesPanel.svelte";

const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    CompanionDataTypeRulesPanel
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
    id: "waltzCompanionDataTypeRulesSection",
    component
}