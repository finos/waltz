import template from "./report-grid-view-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import * as _ from "lodash";
import ReportGridPicker from "../svelte/ReportGridPicker.svelte";

const bindings = {
    parentEntityRef: "<"
};

const initData = {
    showPicker: false,
    ReportGridPicker
};

const localStorageKey = "waltz-report-grid-view-section-last-id";

function controller($scope, serviceBroker, localStorageService) {

    const vm = initialiseData(this, initData);

    vm.$onChanges = () => {
        const lastUsedGridId = localStorageService.get(localStorageKey);

        if (_.isNil(lastUsedGridId)){
            vm.showPicker = true;
        } else {
            serviceBroker
                .loadViewData(CORE_API.ReportGridStore.findAll, [], { force: true })
                .then(r => {
                    vm.selectedGrid = _.find(r.data, d => d.id === lastUsedGridId);
                    if (!vm.selectedGrid){
                        vm.showPicker = true;
                    }
                })
        }
    };

    vm.onGridSelect = (grid) => {
        $scope.$applyAsync(() => {
            console.log({grid})
            localStorageService.set(localStorageKey, grid.id);
            vm.selectedGrid = grid;
            console.log({selectedGrid})
            vm.showPicker = false;
        });
    };
}

controller.$inject = [
    "$scope",
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