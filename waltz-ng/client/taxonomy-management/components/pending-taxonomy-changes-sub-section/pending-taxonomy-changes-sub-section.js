import template from "./pending-taxonomy-changes-sub-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineColorOfSubmitButton} from "../../../common/severity-utils";


const bindings = {
    changeDomain: "<",
};


const initialState = {
    pendingChanges: [],
    selectedPendingChange: null,
    preview: null
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    function loadChanges() {
        if (!vm.changeDomain) return Promise.resolve([]);
        return serviceBroker
            .loadViewData(
                CORE_API.TaxonomyManagementStore.findPendingChangesByDomain,
                [ vm.changeDomain ],
                { force: true })
            .then(r => r.data);
    }

    vm.$onChanges = (c) => {
        if (c.changeDomain) {
            loadChanges()
                .then(cs => vm.pendingChanges = cs);
        }
    };

    vm.onSelectPendingChange = (pendingChange) => {
        vm.selectedPendingChange = pendingChange;
        serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.previewByChangeId,
                [ pendingChange.id ],
                { force: true })
            .then(r => {
                vm.preview = r.data;
                vm.submitButtonClass = determineColorOfSubmitButton(_.map(vm.preview.impacts, "severity"));
            });
    };

    vm.onDismiss = () => {
        vm.preview = null;
        vm.selectedPendingChange = null;
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzPendingTaxonomyChangesSubSection"
}

