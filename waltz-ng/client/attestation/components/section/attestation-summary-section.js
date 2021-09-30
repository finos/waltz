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

import template from "./attestation-summary-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {determineDownwardsScopeForKind, mkSelectionOptions} from "../../../common/selector-utils";
import {attestationPieConfig} from "../../attestation-pie-utils";
import {attestationSummaryColumnDefs} from "../../attestation-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";
import {lifecyclePhase} from "../../../common/services/enums/lifecycle-phase";
import * as _ from "lodash";
import {criticality} from "../../../common/services/enums/criticality";
import {attestationStatus} from "../../../common/services/enums/attestation-status";


const initialState = {
    columnDefs: attestationSummaryColumnDefs,
    selectedLifecycle: null,
    selectedCriticality: null,
    activeTab: "summary",
    gridData: [],
    gridFilters: {},
    selectedTab: null,
    selectedDate: null,
    selectedStatus: null,
    editingDate: false
};


const bindings = {
    parentEntityRef: "<",
    filters: "<",
};

const ALL_LIFECYCLES = 0;
const ALL_CRITICALITIES = 0;
const ALL_STATUSES = 0;



function determineName(summary, categoriesById) {
    switch (summary.attestedKind) {
        case "LOGICAL_DATA_FLOW":
            return "Logical Flows";
        case "PHYSICAL_FLOW":
            return "Physical Flows"
        case "MEASURABLE_CATEGORY":
            return _.get(categoriesById, [summary.attestedId, "name"], "unknown category");
        default:
            throw "Cannot determine name for unknown attested entity kind: " + summary.attestedKind
    }
}

function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadGridData = (attestedKind, attestedId) => {
        serviceBroker
            .loadViewData(
                CORE_API.AttestationInstanceStore.findApplicationInstancesForKindAndSelector,
                [attestedKind, attestedId, vm.appAttestationInfo])
            .then(r => vm.gridData = r.data);
    }

    const loadSummaryData = () => {

        const attestationSummaryPromise = serviceBroker
            .loadViewData(
                CORE_API.AttestationInstanceStore.findApplicationAttestationSummary,
                [vm.appAttestationInfo])
            .then(r => r.data);

        const measurableCategoriesPromise = serviceBroker
            .loadAppData(CORE_API.MeasurableCategoryStore.findAll)
            .then(r => r.data);

        $q.all([attestationSummaryPromise, measurableCategoriesPromise])
            .then(([summaryInfo, measurableCategories]) => {

                const categoriesById = _.keyBy(measurableCategories, c => c.id);

                vm.attestationSummaries = _
                    .chain(summaryInfo)
                    .map(i => Object.assign({}, i, {name: determineName(i, categoriesById), key: `${i.attestedKind}_${i.attestedId}`}))
                    .sortBy("name")
                    .value();
            });
    };

    vm.$onInit = () => {

        vm.lifecycleOptions = _.concat(ALL_LIFECYCLES, _.values(_.mapValues(lifecyclePhase, function(l) { return l.key })));
        vm.selectedLifecycle = ALL_LIFECYCLES;

        vm.criticalityOptions = _.concat(ALL_CRITICALITIES, _.values(_.mapValues(criticality, function(l) { return l.key })));
        vm.selectedCriticality = ALL_CRITICALITIES;

        vm.statusOptions = _.concat(ALL_CRITICALITIES, _.values(_.mapValues(attestationStatus, function(l) { return l.key })));
        vm.selectedStatus = ALL_STATUSES;

        vm.config = attestationPieConfig;

        loadSummaryData();
    };

    vm.$onChanges = (changes) => {

        vm.selectionOptions = mkSelectionOptions(
            vm.parentEntityRef,
            determineDownwardsScopeForKind(vm.parentEntityRef.kind),
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        if(changes.filters) {
            vm.loadData();
        }
    };

    vm.changeTab = (summary) => {
        vm.selectedTab = summary;
        vm.loadData();
    }

    vm.clearAllFilters = () => {
        vm.selectedDate = null;
        vm.selectedStatus = ALL_STATUSES;
        vm.selectedLifecycle = ALL_LIFECYCLES;
        vm.selectedCriticality = ALL_CRITICALITIES
        vm.gridFilters = {};
        vm.loadData();
    };

    vm.onChangeLifecycle = (lifecycle) => {
        vm.gridFilters.appLifecyclePhase = lifecycle === ALL_LIFECYCLES ? null : lifecycle;
        vm.selectedLifecycle = lifecycle;
        vm.loadData();
    };

    vm.onChangeCriticality = (criticality) => {
        vm.gridFilters.appCriticality = criticality === ALL_CRITICALITIES ? null : criticality;
        vm.selectedCriticality = criticality;
        vm.loadData();
    };

    vm.onChangeStatus = (status) => {
        vm.gridFilters.attestationState = status === ALL_STATUSES ? null : status;
        vm.selectedStatus = status;
        vm.loadData();
    };

    vm.toggleEditDate = () => {
        vm.editingDate = true;
    }

    vm.saveDate = () => {
        vm.gridFilters.attestationsFromDate = vm.selectedDate;
        vm.editingDate = false;
        vm.loadData();
    }

    vm.loadData = () => {
        vm.appAttestationInfo = {
            selectionOptions: vm.selectionOptions,
            filters: vm.gridFilters
        }

        if(!_.isEmpty(vm.selectedTab)){
            loadGridData(vm.selectedTab.attestedKind, vm.selectedTab.attestedId);
        } else {
            loadSummaryData();
        }
    }

    vm.filtersApplied = () => {
        return vm.selectedStatus || vm.selectedCriticality || vm.selectedLifecycle || vm.selectedDate
    }
}


controller.$inject = [
    "$q",
    "ServiceBroker",
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzAttestationSummarySection",
};