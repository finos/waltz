import template from "./flow-classification-rule-view.html"
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import FlowClassificationRuleOverview from "../../components/svelte/FlowClassificationRuleOverview.svelte"

const bindings = {

}

const initialState = {
    FlowClassificationRuleOverview,
    parentEntityRef: null
}


const addToHistory = (historyStore, id, name) => {
    if (! id) { return; }
    historyStore.put(
        `Flow classification rule: ${name}`,
        "FLOW_CLASSIFICATION_RULE",
        "main.flow-classification-rule.view",
        { id });
};


function controller($q, $stateParams, serviceBroker, historyStore){

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

        const ruleId = $stateParams.id;

        vm.parentEntityRef = {
            kind: "FLOW_CLASSIFICATION_RULE",
            id: ruleId,
            name: "?"
        };

        const dataTypesPromise = serviceBroker
            .loadAppData(
                CORE_API.DataTypeStore.findAll)
            .then(r => _.keyBy(r.data, d => d.id));

        const rulePromise = serviceBroker
            .loadViewData(
                CORE_API.FlowClassificationRuleStore.getById,
                [ruleId])
            .then(r =>  r.data);

        $q.all([dataTypesPromise, rulePromise])
            .then(([dataTypesById, rule]) => {

                const datatype = dataTypesById[rule.dataTypeId];
                const flowClassificationRuleName =
                    rule.subjectReference.name
                    + " - "
                    + _.get(datatype, "name", "unknown datatype");

                vm.parentEntityRef = {
                    kind: "FLOW_CLASSIFICATION_RULE",
                    id: ruleId,
                    name: flowClassificationRuleName
                };

                addToHistory(
                    historyStore,
                    rule.id,
                    flowClassificationRuleName);
            });
    };
}

controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker",
    "HistoryStore"
];


const component = {
    template,
    bindings,
    controller
}

export default {
    component,
    id: "waltzFlowClassificationRuleView"
}