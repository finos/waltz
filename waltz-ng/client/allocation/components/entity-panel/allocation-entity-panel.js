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
    editing: true,
    saveEnabled: true,
    dirty: false
};


function calcTotal(enrichedAllocations = []) {
    return _.sumBy(
        enrichedAllocations,
        d => d.allocation.percentage);
}


function controller($q, notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    // -- UTILS --
    function loadData() {
        const measurablePromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => _.keyBy(r.data, "id"));

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
            .then(([measurablesById, scheme, rawAllocations]) => {
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

        vm.fixedTotal = calcTotal(vm.fixedAllocations);
        vm.floatingTotal = calcTotal(vm.floatingAllocations);
        vm.total = vm.fixedTotal + vm.floatingTotal;

        _.forEach(vm.enrichedAllocations, ea => {
            const percentageChanged = ea.working.percentage !== ea.allocation.percentage;
            const typeChanged = ea.working.type !== ea.allocation.type;
            ea.working.dirty = percentageChanged || typeChanged
        });
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

    vm.onUpdateTypeOld = (d, type) => {
        const niceType = type === "FIXED"
            ? "fixed"
            : "floating";

        serviceBroker
            .execute(
                CORE_API.AllocationStore.updateType,
                [vm.entityReference, vm.schemeId, d.measurable.id, type])
            .then(r => {
                if (r.data === true) {
                    notification.success(`Converted ${d.measurable.name} to ${niceType}`);
                } else {
                    notification.warning(`Could not convert ${d.measurable.name} to ${niceType}, it may have been removed or already converted`);
                }
                reload();
            })
            .catch(e => notification.error(`Could not convert ${d.measurable.name} to ${niceType}`));
    };

    vm.onUpdateType = (d, type) => {
        d.working.type = type;
        recalcData();
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
                    notification.success("Updated percentage allocations");
                } else {
                    notification.warning("Could not update percentages");
                }
                reload();
                vm.setEditable(false);
            })
            .catch(e => notification.error("Could not update percentages"));
    };

    vm.setEditable = (targetState) => {
        vm.editing = targetState;
    };

    vm.validateAllocations = () => {
        const hasFloats = vm.floatingAllocations.length > 0;
        vm.saveEnabled = true;

        vm.fixedAllocations = _.each(vm.fixedAllocations, (fa,i) => {
            const totalFixed = _.sumBy(vm.fixedAllocations, fa => fa.working.percentage);

            if (fa.working.percentage > 100) {
                fa.working.status = "FAIL";
                fa.working.message = "Cannot exceed 100%";
                vm.saveEnabled = false;
            } else if (totalFixed > 100) {
                fa.working.status = "WARN";
                fa.working.message = "Total exceeds 100%";
                vm.saveEnabled = false;
            } else if (totalFixed < 100 && ! hasFloats) {
                fa.working.status = "WARN";
                fa.working.message = "Total does make 100%";
                vm.saveEnabled = false;
            }else {
                fa.working.status = "OK";
                fa.working.message = "";
            }

            //fa.working.status = ["FAIL","OK","WARN"][i%3];
        })



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