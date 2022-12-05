/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import template from "./allocation-entity-sub-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {calcWorkingTotal, determineChangeType, updateDirtyFlags, validateItems} from "../../allocation-utilities";
import _ from "lodash";
import {displayError} from "../../../common/error-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";
import toasts from "../../../svelte-stores/toast-store";

const bindings = {
    entityReference: "<",
    scheme: "<",
    measurableCategory: "<",
    allocations: "<",
    onSave: "<",
    onDismiss: "<",
    filters: "<",
    canEdit: "<"
};


const initialState = {
    allocated: [],
    editing: false,
    saveEnabled: false,
    scheme: null,
    showingHelp: false,
    unallocated: [],
    canEdit: false
};


function findMeasurablesRelatedToScheme(ratings = [], measurablesById = {}, scheme) {
    return _
        .chain(ratings)
        .map(r => measurablesById[r.measurableId])
        .compact()
        .filter(m => m.categoryId === scheme.measurableCategoryId)
        .value();
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);
    let items = [];

    // -- UTILS --
    function loadData() {
        const measurablePromise = serviceBroker
            .loadAppData(CORE_API.MeasurableStore.findAll)
            .then(r => r.data);

        const ratingsPromise = serviceBroker
            .loadViewData(
                CORE_API.MeasurableRatingStore.findForEntityReference,
                [vm.entityReference])
            .then(r => r.data);

        return $q
            .all([measurablePromise, ratingsPromise])
            .then(([allMeasurables, ratings]) => {
                const measurablesById = _.keyBy(allMeasurables, "id");
                const ratingsByMeasurableId = _.keyBy(ratings, "measurableId");
                const availableMeasurables = findMeasurablesRelatedToScheme(ratings, measurablesById, vm.scheme);
                const allocationsByMeasurableId = _
                    .chain(vm.allocations)
                    .filter(a => a.schemeId === vm.scheme.id)
                    .keyBy(a => a.measurableId)
                    .value();

                items = _
                    .chain(availableMeasurables)
                    .map(measurable => {
                        const allocation = allocationsByMeasurableId[measurable.id];
                        const working = {
                            isAllocated: !_.isNil(allocation),
                            dirty: false,
                            percentage: _.get(allocation, "percentage", 0)
                        };
                        return {
                            allocation,
                            measurable,
                            working,
                            rating: ratingsByMeasurableId[measurable.id]
                        };
                    })
                    .value();
            });
    }


    function recalcData() {
        const [allocated, unallocated] = _
            .chain(items)
            .orderBy(d => d.measurable.name)
            .partition(d => d.working.isAllocated)
            .value();

        const hasDirtyData = updateDirtyFlags(items);
        const validAllocations = validateItems(items);

        vm.saveEnabled = validAllocations && hasDirtyData;

        vm.allocatedTotal = calcWorkingTotal(allocated);
        vm.remainder = 100 - vm.allocatedTotal;
        vm.allocated = allocated;
        vm.unallocated = unallocated;
    }


    function reload() {
        return loadData()
            .then(recalcData);
    }


    // -- LIFECYCLE

    vm.$onInit = () => {
    };

    vm.$onChanges = () => {
        if (vm.scheme && vm.allocations) {
            reload();
        }
        if(vm.entityReference){
            vm.selector = mkSelectionOptions(
                vm.entityReference,
                undefined,
                [entityLifecycleStatus.ACTIVE.key],
                vm.filters);
        }
    };

    vm.$onDestroy = () => {
    };


    // -- INTERACT

    vm.onMoveToAllocated = (d) => {
        d.working = {
            isAllocated: true,
            percentage: 0
        };
        recalcData();
    };

    vm.onMoveToUnallocated = (d) => {
        d.working = {
            isAllocated: false,
            percentage: 0
        };
        recalcData();
    };

    vm.onGrabUnallocated = (d) => {
        d.working.percentage = d.working.percentage + vm.remainder;
        recalcData();
    };

    vm.onZeroAndDistribute = (d) => {
        const amountToDistribute = d.working.percentage;

        const validRecipients = _
            .chain(vm.allocated)
            .filter(a => a.measurable.id !== d.measurable.id)
            .filter(a => a.working.percentage !== 0)
            .orderBy(a => a.working.percentage, "desc")
            .value();

        const currentTotal = _.sumBy(validRecipients, d => d.working.percentage);

        if (currentTotal > 0) {
            let amountGiven = 0;
            validRecipients.forEach((d) => {
                const amountToGive = Math.floor((amountToDistribute / currentTotal) * d.working.percentage);
                amountGiven = amountGiven += amountToGive;
                d.working.percentage += amountToGive;
            });

            const remainder = amountToDistribute - amountGiven;
            validRecipients[0].working.percentage += remainder;
        }

        d.working.percentage = 0;
        recalcData();
    };

    vm.onPercentageChange = () => recalcData();

    vm.onPercentageFocusLost = () => {
        _.each(items, d => {
            if (_.isNil(d.working.percentage)) {
                d.working.percentage = 0;
            }
            if (!_.isInteger(d.working.percentage)) {
                const rounded = Math.round(d.working.percentage);
                toasts.warning(`Allocations must be whole numbers, therefore rounding: ${d.working.percentage} to: ${rounded}`);
                d.working.percentage = rounded;
            }
        });
        recalcData();
    };

    vm.onSavePercentages = () => {
        const changes = _
            .chain(items)
            .filter(d => d.working.dirty)
            .map(d => ({
                operation: determineChangeType(d),
                measurablePercentage: {
                    measurableId: d.measurable.id,
                    percentage: d.working.percentage
                },
                previousPercentage: d.allocation == null ? undefined : d.allocation.percentage
            }))
            .value();

        vm.onSave(changes)
            .then(r => {
                if (r === true) {
                    toasts.success("Updated allocations");
                } else {
                    toasts.warning("Could not update allocations");
                }
                reload();
                vm.setEditable(false);
            })
            .catch(e => displayError("Could not update allocations", e));
    };

    vm.setEditable = (targetState) => {
        vm.editing = targetState;
    };

    vm.onCancel = () => {
        vm.setEditable(false);
        return reload()
            .then(() => toasts.info("Edit cancelled: reverting to last saved"));
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


const component = {
    bindings,
    controller,
    template
};


export default {
    component,
    id: "waltzAllocationEntitySubSection"
};