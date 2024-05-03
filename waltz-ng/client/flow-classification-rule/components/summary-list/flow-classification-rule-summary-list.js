import template from "./flow-classification-rule-summary-list.html"
import {initialiseData} from "../../../common";
import FlowClassificationRulesPanel from "./FlowClassificationRulesPanel.svelte";

const bindings = {}

const initialState = {
    FlowClassificationRulesPanel,
};

function controller() {
    const vm = initialiseData(this, initialState);
}

controller.$inject = [];

const component = {
    template,
    controller,
    bindings
};


export default {
    id: "waltzFlowClassificationRuleSummaryList",
    component
}