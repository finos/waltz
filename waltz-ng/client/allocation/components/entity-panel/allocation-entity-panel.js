import template from "./allocation-entity-panel.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {
    calcWorkingTotal,
    updateDirtyFlags,
    updateFloatingValues,
    validateAllocations
} from "../../allocation-utilities";
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
    editing: false,
    saveEnabled: false,
    showingHelp: false,
    dirty: false
};


function controller($q, notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    // -- UTILS --
    function loadData() {
        const measurablePromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => r.data);

        const schemePromise = serviceBroker
            .loadViewData(
                CORE_API.AllocationSchemeStore.getById,
                [vm.schemeId])
            .then(r => r.data);

        const allocationPromise = serviceBroker
            .loadViewData(
                CORE_API.AllocationStore.findByEntityAndScheme,
                [vm.entityReference, vm.schemeId],
                { force: true })
            .then(r => r.data);

        return $q
            .all([measurablePromise, schemePromise, allocationPromise])
            .then(([measurables, scheme, rawAllocations]) => {
                const measurablesById = _.keyBy(measurables, "id");
                vm.scheme = scheme;
                vm.enrichedAllocations = _
                    .chain(rawAllocations)
                    .map(allocation => {
                        const measurable = measurablesById[allocation.measurableId];
                        const working = {
                            dirty: false,
                            type: allocation.type,
                            percentage: allocation.percentage
                        };
                        return Object.assign({}, {allocation, measurable, working});
                    })
                    .value();
            })
    }



    function recalcData() {
        const allocationsByType = _
            .chain(vm.enrichedAllocations)
            .orderBy(d => d.measurable.name)
            .groupBy(d => d.working.type)
            .value();

        vm.fixedAllocations = _.get(allocationsByType, "FIXED", []);
        vm.floatingAllocations = _.get(allocationsByType, "FLOATING", []);

        vm.fixedTotal = calcWorkingTotal(vm.fixedAllocations);
        vm.total = vm.fixedTotal + vm.floatingTotal;
        vm.floatingTotal = 100 - vm.fixedTotal;

        updateFloatingValues(vm.floatingTotal, vm.floatingAllocations);

        const hasDirtyData = updateDirtyFlags(vm.enrichedAllocations);

        const validAllocations = validateAllocations(
            vm.fixedAllocations,
            vm.floatingAllocations);

        vm.saveEnabled = validAllocations && hasDirtyData;
    }


    function reload() {
        return loadData()
            .then(recalcData);
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
        d.working.type = type;
        if (type === "FIXED") {
            d.working.percentage = Math.floor(d.working.percentage);
        }
        recalcData();
    };

    vm.onGrabFloat = (d) => {
        d.working.percentage = d.working.percentage + vm.floatingTotal;
        recalcData();
    };

    vm.onUpdatePercentages = () => {
        const allocationsToSave = _.map(
                vm.fixedAllocations,
                fa => {
                    return {
                        measurableId: fa.measurable.id,
                        percentage: fa.working.percentage
                    };
                });

        serviceBroker
            .execute(CORE_API.AllocationStore.updateFixedAllocations,
                [vm.entityReference, vm.schemeId, allocationsToSave])
            .then(r => {
                if (r.data === true) {
                    notification.success("Updated percentage allocations");
                } else {
                    notification.warning("Could not update percentages");
                }
                reload();
                vm.setEditable(false);
            })
            .catch(e => notification.error("Could not update percentages"));
    };

    vm.setEditable = (targetState, cancelChanges) => {
        vm.editing = targetState;
        reload();
    };

    vm.onPercentageChange = () => recalcData();

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