import template from "./roadmap-scenario-axis-config.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    axisDefinitions: "<",
    axisOrientation: "@",
    axisType: "<",
    onSave: "<",
};


const viewTab = {
    id: "VIEW",
    name: "View"
};


const pickTab = {
    id: "PICK",
    name: "Pick"
};


const sortTab = {
    id: "SORT",
    name: "Sort"
};


const initialState = {
    tabs: [ viewTab, pickTab, sortTab ],
    activeTabId: viewTab.id
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);
    global.vm = vm;

    function prepareData() {
        serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => {
                vm.allItems = _
                    .chain(r.data)
                    .filter(m => m.categoryId === vm.axisType.id)
                    .map(m => Object.assign({}, m, { concrete: true }))
                    .value();

                vm.usedItems = _
                    .chain(vm.axisDefinitions)
                    .filter(d => d.axisKind === vm.axisOrientation)
                    .map()
            });

    }
    vm.$onChanges = (c) => {
        if (vm.axisType && vm.axisDefinitions) {
            prepareData();
        }
    };

    // -- interact --

    vm.onCancel = () => {
        vm.activeTabId = viewTab.id;
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};


const id = "waltzRoadmapScenarioAxisConfig";


export default {
    component,
    id
};