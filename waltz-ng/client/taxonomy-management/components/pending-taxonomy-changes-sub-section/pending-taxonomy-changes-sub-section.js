import template from "./pending-taxonomy-changes-sub-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    changeDomain: "<",
};


const initialState = {
    pendingChanges: [],
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
        console.log('s', c);

        serviceBroker
            .execute(
                CORE_API.TaxonomyManagementStore.previewByChangeId,
                [ c.id ],
                { force: true })
            .then(r => vm.preview = r.data);

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

