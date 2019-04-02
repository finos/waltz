import template from "./allocation-entity-panel.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";

import _ from "lodash";


const bindings = {
    entityReference: "<",
    schemeId: "<"
};


const initialState = {
    scheme: null,
    rawAllocations: [],
    fixedAllocations: [],
    floatingAllocations: []
};


function calcTotal(enrichedAllocations = []) {
    return _.sumBy(
        enrichedAllocations,
        d => d.allocation.percentage);
}


function controller($q, notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    global.vm = vm;

    // -- UTILS --
    function loadData() {
        const measurablePromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => vm.measurablesById = _.keyBy(r.data, "id"));

        const schemePromise = serviceBroker
            .loadViewData(
                CORE_API.AllocationSchemeStore.getById,
                [vm.schemeId])
            .then(r => vm.scheme = r.data);

        const allocationPromise = serviceBroker
            .loadViewData(
                CORE_API.AllocationStore.findByEntityAndScheme,
                [vm.entityReference, vm.schemeId],
                { force: true })
            .then(r => vm.rawAllocations = r.data);

        return $q.all([measurablePromise, schemePromise, allocationPromise]);
    }

    function prepareData() {
        [vm.fixedAllocations, vm.floatingAllocations] = _
            .chain(vm.rawAllocations)
            .map(allocation => {
                const measurable = vm.measurablesById[allocation.measurableId];
                return Object.assign({}, {allocation, measurable});
            })
            .partition(d => d.allocation.isFixed)
            .value();

        vm.fixedTotal = calcTotal(vm.fixedAllocations);
        vm.floatingTotal = calcTotal(vm.floatingAllocations);
        vm.total = vm.fixedTotal + vm.floatingTotal;
    }

    function reload() {
        return loadData()
            .then(prepareData);
    }

    // -- LIFECYCLE

    vm.$onInit = () => {
        reload();
    };

    vm.$onChanges = (c) => {
    };

    vm.$onDestroy = () => {
    };

    // -- INTERACT

    vm.onMakeFixed = (d) => {
        serviceBroker
            .execute(
                CORE_API.AllocationStore.makeFixed,
                [vm.entityReference, vm.schemeId, d.measurable.id])
            .then(r => {
                console.log(r);
                if (r.data === true) {
                    notification.success(`Converted ${d.measurable.name} to fixed`);
                } else {
                    notification.warning(`Could not convert ${d.measurable.name} to fixed, it may have been removed or already converted`);
                }
                reload();
            })
            .catch(e => notification.error(`Could not convert ${d.measurable.name} to fixed`));
        console.log("Make fixed:", d);
    }
}


controller.$inject = [
    "$q",
    "Notification",
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: "waltzAllocationEntityPanel"
};