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
    floatingAllocations: [],
    editing: false
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
        const allocationsByType  = _
            .chain(vm.rawAllocations)
            .map(allocation => {
                const measurable = vm.measurablesById[allocation.measurableId];
                const working = { editing: false, percentage: allocation.percentage };
                return Object.assign({}, {allocation, measurable, working});
            })
            .orderBy(d => d.measurable.name)
            .groupBy(d => d.allocation.type)
            .value();

        vm.fixedAllocations = _.get(allocationsByType, "FIXED", []);
        vm.floatingAllocations = _.get(allocationsByType, "FLOATING", []);

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

    vm.onUpdateType = (d, type) => {
        const niceType = type === 'FIXED'
            ? "fixed"
            : "floating";

        serviceBroker
            .execute(
                CORE_API.AllocationStore.updateType,
                [vm.entityReference, vm.schemeId, d.measurable.id, type])
            .then(r => {
                console.log(r);
                if (r.data === true) {
                    notification.success(`Converted ${d.measurable.name} to ${niceType}`);
                } else {
                    notification.warning(`Could not convert ${d.measurable.name} to ${niceType}, it may have been removed or already converted`);
                }
                reload();
            })
            .catch(e => notification.error(`Could not convert ${d.measurable.name} to ${niceType}`));
    };

    vm.onUpdatePercentages = () => {
        const percentages = _.map(
                vm.fixedAllocations,
                fa => {
                    return {
                        measurableId: fa.measurable.id,
                        percentage: fa.working.percentage
                    };
                });

        serviceBroker
            .execute(CORE_API.AllocationStore.updatePercentages,
                [vm.entityReference, vm.schemeId, percentages])
            .then(r => {
                if (r.data === true) {
                    notification.success(`Updated percentage allocations`);
                } else {
                    notification.warning(`Could not update percentages`);
                }
                reload();
                vm.setEditable(false);
            })
            .catch(e => notification.error(`Could not update percentages`));
    };

    vm.setEditable = (targetState) => {
        vm.editing = targetState;
    };
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