import UserDashboardView from "../svelte/user-dashboard/UserDashboardView.svelte";
import template from "./user-dashboard-view.html";
import {initialiseData} from "../../../common";

const initialState = {
    UserDashboardView
}

function controller($q, $stateParams, serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {

    }
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
