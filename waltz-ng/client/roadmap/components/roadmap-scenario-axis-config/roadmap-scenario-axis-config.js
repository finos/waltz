import template from "./roadmap-scenario-axis-config.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {move} from "../../../common/list-utils";


const bindings = {
    scenarioId: "<",
    axisOrientation: "@",
    axisDomain: "<", // ref
    onAddAxisItem: "<",
    onRemoveAxisItem: "<",
    onRepositionAxisItems: "<",
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


function controller($q, serviceBroker, notification) {
    const vm = initialiseData(this, initialState);

    function prepareData() {
        if (! vm.usedItems) return;
        if (! vm.axisDomain) return;
        if (! vm.measurablesByCategory) return;

        if (! vm.allItems) vm.allItems = vm.measurablesByCategory[vm.axisDomain.id];
        const allItemsById = _.keyBy(vm.allItems, "id");

        vm.checkedItemIds = _.map(vm.usedItems, d => d.domainItem.id);
        vm.expandedItemIds = _
            .chain(vm.checkedItemIds)
            .map(d => allItemsById[d])
            .compact()
            .map("parentId")
            .uniq()
            .value();
    }

    function reloadData() {
        serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.loadAxis,
                [ vm.scenarioId, vm.axisOrientation ],
                { force: true })
            .then(r => vm.usedItems = r.data)
            .then(() => prepareData());
    }

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurablesByCategory = _
                .chain(r.data)
                .map(m => Object.assign({}, m, {concrete: true})) // make all concrete so anything can be selected
                .groupBy(m => m.categoryId)
                .value())
            .then(() => prepareData());
    };

    vm.$onChanges = () => {
        if (vm.axisDomain) {
            reloadData();
        }
    };


    // -- interact --

    vm.onCancel = () => {
        vm.activeTabId = viewTab.id;
    };

    vm.onItemCheck = (checkedId) => {
        const highestPosition = _.get(
            _.maxBy(vm.usedItems, d => d.position),
            "position",
            0);
        const newPosition = highestPosition + 10;

        const newItem = {
            orientation: vm.axisOrientation,
            domainItem: {
                id: checkedId,
                kind: "MEASURABLE"
            },
            position: newPosition
        };

        vm.onAddAxisItem(newItem)
            .then(() => reloadData());
    };

    vm.onItemToggle = (toggledId) => {
        const isExistingItem = _.some(
            vm.usedItems,
            d => d.domainItem.id === toggledId);

        if (isExistingItem) {
            vm.onItemUncheck(toggledId);
        } else {
            vm.onItemCheck(toggledId);
        }
    };

    vm.onItemUncheck = (uncheckedId) => {
        const itemToRemove = {
            orientation: vm.axisOrientation,
            domainItem: {
                id: uncheckedId,
                kind: "MEASURABLE"
            }
        };
        vm.onRemoveAxisItem(itemToRemove)
            .then(() => reloadData());
    };

    vm.onMoveUp = (id) => {
        const position = _.findIndex(vm.usedItems, d => d.id === id);
        vm.usedItems = move(vm.usedItems, position, -1);
    };

    vm.onMoveDown = (id) => {
        const position = _.findIndex(vm.usedItems, d => d.id === id);
        vm.usedItems = move(vm.usedItems, position, 1);
    };

    vm.onMoveTop = (id) => {
        const pred = d => d.id === id;
        const item = _.find(vm.usedItems, pred);
        vm.usedItems = [item].concat(_.reject(vm.usedItems, pred));
    };

    vm.onMoveBottom = (id) => {
        const pred = d => d.id === id;
        const item = _.find(vm.usedItems, pred);
        vm.usedItems = _.reject(vm.usedItems, pred).concat([item]);
    };

    vm.onSortAlphabetically = () => {
        vm.usedItems = _.sortBy(vm.usedItems, d => d.domainItem.name.toLowerCase());
    };

    vm.onSaveSort = () => {
        const orderedIds = _.map(vm.usedItems, d => d.id);
        vm.onRepositionAxisItems(vm.scenarioId, vm.axisOrientation, orderedIds)
            .then(() => reloadData())
            .then(() => notification.success("Axis reordered"));
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "Notification"
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