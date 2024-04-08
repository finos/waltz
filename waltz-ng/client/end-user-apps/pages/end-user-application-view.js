import EndUserApplicationOverview
    from "../svelte/EndUserApplicationOverview.svelte";
import {initialiseData} from "../../common";
import {CORE_API} from "../../common/services/core-api-utils";

const initialState = {
    EndUserApplicationOverview
};


const addToHistory = (historyStore, ref) => {
    if (! ref) { return; }
    historyStore.put(
        ref.name,
        "END_USER_APPLICATION",
        "main.end-user-application.view",
        { id: ref.id });
};


function controller($stateParams, historyStore, serviceBroker) {

    const vm = initialiseData(this, initialState);

    const endUserAppId = $stateParams.id;

    vm.parentEntityRef = { id: endUserAppId, kind: "END_USER_APPLICATION" };

    serviceBroker
        .loadViewData(
            CORE_API.EndUserAppStore.getById,
            [endUserAppId])
        .then(r => {
            const endUserApp = r.data;
            vm.parentEntityRef = Object.assign({}, vm.parentEntityRef, endUserApp);
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