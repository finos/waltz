import EndUserApplicationOverview
    from "../svelte/EndUserApplicationOverview.svelte";
import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";

const initialState = {
    EndUserApplicationOverview
};


const addToHistory = (historyStore, rating) => {
    if (! rating) { return; }
    historyStore.put(
        rating.name,
        "END_USER_APPLICATION",
        "main.end-user-application-view.view",
        { id: rating.id });
};


function controller($stateParams, historyStore, serviceBroker) {

    console.log("EndUserApp ng controller")
    const vm = initialiseData(this, initialState);

    const endUserAppId = $stateParams.id;

    vm.parentEntityRef = null;

    serviceBroker
        .loadViewData(
            CORE_API.EndUserAppStore.getById,
            [endUserAppId])
        .then(r => {
            const endUserApp = r.data;
            vm.parentEntityRef = endUserApp;
            addToHistory(historyStore, vm.parentEntityRef);
        });
}


controller.$inject = [
    "$stateParams",
    "HistoryStore",
    "ServiceBroker"
];

const template = `
    <div>
        <waltz-svelte-component component="$ctrl.EndUserApplicationOverview"
                                primary-entity-reference="$ctrl.parentEntityRef">
        </waltz-svelte-component>

        <br>

        <waltz-dynamic-sections-view parent-entity-ref="$ctrl.parentEntityRef">
        </waltz-dynamic-sections-view>
    </div>`;

export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
};