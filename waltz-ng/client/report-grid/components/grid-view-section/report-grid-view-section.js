import template from "./report-grid-view-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import * as _ from "lodash";

const bindings = {
    parentEntityRef: "<"
};

const initData = {
    showPicker: false
};

const localStorageKey = "waltz-report-grid-view-section-last-id";

function controller(serviceBroker, localStorageService) {

    const vm = initialiseData(this, initData);

    vm.$onChanges = () => {
        const lastUsedGridId = localStorageService.get(localStorageKey);

        if (_.isNil(lastUsedGridId)){
            vm.showPicker = true;
        } else {
            serviceBroker
                .loadViewData(CORE_API.ReportGridStore.findAll)
                .then(r => {
                    vm.selectedGrid = _.find(r.data, d => d.id === lastUsedGridId);
                    if (!vm.selectedGrid){
                        vm.showPicker = true;
                    }
                })
        }
    };

    vm.onGridSelect = (grid) => {
        localStorageService.set(localStorageKey, grid.id);
        vm.selectedGrid = grid;
        vm.showPicker = false;
    };
}

controller.$inject = [
    "ServiceBroker",
    "localStorageService"
];

const component = {
    controller,
    bindings,
    template
};

export default {
    id: "waltzReportGridViewSection",
    component,
}