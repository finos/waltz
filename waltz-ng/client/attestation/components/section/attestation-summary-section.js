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
import {attestationPieConfig, prepareSummaryData} from "../../attestation-pie-utils";
import {attestationSummaryColumnDefs} from "../../attestation-utils";
import {entityLifecycleStatus} from "../../../common/services/enums/entity-lifecycle-status";
import {lifecyclePhase} from "../../../common/services/enums/lifecycle-phase";
import * as _ from "lodash";
import moment from "moment";


const initialState = {
    columnDefs: attestationSummaryColumnDefs,
    rawGridData: [],
    gridDataToDisplay: [],

    selectedFlowType: null,
    selectedYear: null,
    selectedLifecycle: null,
    selectedSegment: null,
    selectedAttestationType: null,

    extractUrl: null,

    visibility : {
        tableView: false
    },

    activeTab: "summary",
    gridData: [],
    gridFilters: {},
    selectedTab: null
};


const bindings = {
    parentEntityRef: "<",
    filters: "<",
    selectedYear: "<",
    selectedLifecycle: "<",
    selectedAppsByYear: "<"
};

const ALL_YEARS = 0;
const ALL_LIFECYCLES = 0;



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

        vm.appAttestationInfo = {
            selectionOptions: vm.selectionOptions,
            filters: vm.gridFilters
        }

        const attestationInstanceSummaryPromise = serviceBroker
            .loadViewData(
                CORE_API.AttestationInstanceStore.findApplicationInstancesForKindAndSelector,
                [attestedKind, attestedId, vm.appAttestationInfo])
            .then(r => vm.gridData = console.log("done") ||  r.data);
    }

    const loadSummaryData = () => {

        vm.appAttestationInfo = {
            selectionOptions: vm.selectionOptions,
            filters: vm.gridFilters
        }

        const attestationSummaryPromise = serviceBroker
            .loadViewData(
                CORE_API.AttestationInstanceStore.findApplicationAttestationSummary,
                [vm.appAttestationInfo])
            .then(r => console.log("summary", r.data) || r.data);


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

                console.log({gd: vm.gridData});
                console.log({summary: vm.attestationSummaries});
            });
    };


    vm.$onInit = () => {

        const currentYear = moment().year();

        vm.yearOptions = [
            ALL_YEARS,
            currentYear,
            currentYear - 1,
            currentYear - 2,
            currentYear - 3
        ];
        vm.selectedYear = ALL_YEARS;
        vm.lifecycleOptions = _.concat(ALL_LIFECYCLES, _.values(_.mapValues(lifecyclePhase, function(l) { return l.key })));
        vm.selectedLifecycle = ALL_LIFECYCLES;

        vm.config = attestationPieConfig

        loadSummaryData();
    };


    vm.$onChanges = (changes) => {

        vm.selectionOptions = mkSelectionOptions(
            vm.parentEntityRef,
            determineDownwardsScopeForKind(vm.parentEntityRef.kind),
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        if(changes.filters) {
            loadSummaryData();
        }

        if(changes.activeTab){
            loadGridData();
        }
    };

    vm.changeTab = (summary) => {
        vm.selectedTab = summary;

        if(!_.isEmpty(summary)){
            loadGridData(summary.attestedKind, summary.attestedId);
        } else {
            loadSummaryData();
        }
    }

    vm.onChangeYear = (year) => {
        vm.selectedYear = Number(year);
        loadSummaryData();
    };

    vm.onChangeLifecycle = (lifecycle) => {
        vm.gridFilters.appLifecyclePhase = lifecycle;
        vm.selectedLifecycle = lifecycle;

        if(!_.isEmpty(vm.selectedTab)){
            loadGridData(vm.selectedTab.attestedKind, vm.selectedTab.attestedId);
        } else {
            loadSummaryData();
        }
    };
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