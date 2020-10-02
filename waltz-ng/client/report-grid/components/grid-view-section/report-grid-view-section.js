import template from "./report-grid-view-section.html";
import {initialiseData} from "../../../common";

const bindings = {
    parentEntityRef: "<"
};

const initData = {
    gridId: 1,
    showPicker: false
};

function controller(serviceBroker) {

    const vm = initialiseData(this, initData);

    vm.onGridSelect = (grid) => {
        vm.gridId = grid.id;
        vm.showPicker = false;
    };
}

controller.$inject = ["ServiceBroker"];

const component = {
    controller,
    bindings,
    template
};

export default {
    id: "waltzReportGridViewSection",
    component,
}