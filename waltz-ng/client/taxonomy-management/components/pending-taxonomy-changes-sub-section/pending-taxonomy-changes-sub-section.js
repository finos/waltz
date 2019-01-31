import template from "./pending-taxonomy-changes-sub-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineColorOfSubmitButton} from "../../../common/severity-utils";


const bindings = {
    pendingChanges: "<",
    onApplyChange: "<",
    onDiscardChange: "<",
    onDismiss: "<"
};


const modes = {
    LIST: "LIST",
    VIEW: "VIEW"
};


const initialState = {
    selectedPendingChange: null,
    preview: null,
    mode: modes.LIST
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function reload() {
        console.log("reload ?!")
    }

    vm.$onChanges = (c) => {
    };

    vm.onSelectPendingChange = (pendingChange) => {
        vm.selectedPendingChange = pendingChange;
        vm.mode = modes.VIEW;
        serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.previewById,
                [ pendingChange.id ],
                { force: true })
            .then(r => {
                vm.preview = r.data;
                vm.submitButtonClass = determineColorOfSubmitButton(_.map(vm.preview.impacts, "severity"));
            });
    };

    vm.onDismiss = () => {
        vm.mode = modes.LIST;
        vm.preview = null;
        vm.selectedPendingChange = null;
    };

    vm.onDiscardPendingChange = (c) => {
        vm.onDiscardChange(c)
            .then(vm.onDismiss);
    };

    vm.onApplyPendingChange = (c) => {
        vm.onApplyChange(c)
            .then(vm.onDismiss)
            .then(reload);
    };

}


controller.$inject = [
    "Notification",
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

