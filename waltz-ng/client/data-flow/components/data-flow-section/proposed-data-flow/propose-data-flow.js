import ProposeDataFlowView from "../../svelte/propose-data-flow/svelte/ProposeDataFlowView.svelte";
import template from "./propose-data-flow.html";
import {initialiseData} from "../../../../common";
const initialState = {
    ProposeDataFlowView
}

function controller($q, $stateParams, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

    }

    const sourceEntityRef = {
        id: $stateParams.id,
        kind: $stateParams.kind
    };

    vm.parentEntityRef = sourceEntityRef;
    console.log(sourceEntityRef);

}

controller.$inject = [
    "$q",
    "$stateParams",
    "ServiceBroker"
];


const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}};


export default view;
