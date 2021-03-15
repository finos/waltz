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
import {entity} from "../../../common/services/enums/entity";
import {attestationSummaryColumnDefs, mkAttestationSummaryDataForApps} from "../../attestation-utils";
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
    }
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


/**
 * Constructs an export url
 * @param attestationType
 * @param segment (optional)
 * @param year (optional, but needed if segment is provided)
 * @param lifecycle (application lifecycle phase; different from entityLifeCycle)
 * @returns {string}
 */
function mkExtractUrl(attestationType, segment, year, lifecycle) {
    const status = segment.key;
    const yearParam = status === "NEVER_ATTESTED" || year === ALL_YEARS
        ? ""
        : `&year=${year}`;
    const lifecycleParam = status === "NEVER_ATTESTED" || lifecycle === ALL_LIFECYCLES
        ? ""
        : `&lifecycle=${lifecycle}`;

    return `attestations/${attestationType}?status=${status}${yearParam}${lifecycleParam}`;
}


function calcGridData(segment, gridData, year, lifecycle) {
    if (_.isNil(segment)) {
        // return everything as no segments have been selected (i.e. total was clicked)
        return gridData;
    } else if (segment.key === "NEVER_ATTESTED" && lifecycle === 0) {
        // the unattested segment was clicked, so show only rows without an attestation and with all lifecycle phasea
        return _.filter(gridData, d => _.isNil(d.attestation));
    } else if (segment.key === "NEVER_ATTESTED" && lifecycle !== 0) {
        // the unattested segment was clicked, so show only rows without an attestation and with selected lifecycle phase
        return _.filter(gridData, d => _.isNil(d.attestation) && d.application.lifecyclePhase === lifecycle);
    } else if(year === ALL_YEARS && lifecycle === 0){
        // the attested segment was clicked, so show only rows with an attestation and with all lifecycle phasea
        return _.filter(gridData, d => !_.isNil(d.attestation));
    } else if(year === ALL_YEARS && lifecycle !== 0){
        // the attested segment was clicked, so show only rows with an attestation and with selected lifecycle phase
        return _.filter(gridData, d => !_.isNil(d.attestation) && d.application.lifecyclePhase === lifecycle);
    } else if(lifecycle === 0){
        // the attested segment was clicked, so show only rows with an attestation and attestation date in year and with all lifecycle phases
        return _
            .chain(gridData)
            .filter(d => !_.isNil(d.attestation))  // attestation exists
            .filter(d => (moment(d.attestation.attestedAt, "YYYY-MM-DD").year()) === year)
            .value();
    } else{
        // the attested segment was clicked, so show only rows with an attestation and attestation date in year and with selected lifecycle phase
        return _
            .chain(gridData)
            .filter(d => !_.isNil(d.attestation))  // attestation exists
            .filter(d => (moment(d.attestation.attestedAt, "YYYY-MM-DD").year()) === year)
            .filter(d => d.application.lifecyclePhase === lifecycle)
            .value();
    }
}


function controller($q,
                    serviceBroker,
                    displayNameService) {
    const vm = initialiseData(this, initialState);


    const loadData = () => {

        vm.selectionOptions = mkSelectionOptions(
            vm.parentEntityRef,
            determineDownwardsScopeForKind(vm.parentEntityRef.kind),
            [entityLifecycleStatus.ACTIVE.key],
            vm.filters);

        const attestationInstancePromise = serviceBroker
            .loadViewData(
                CORE_API.AttestationInstanceStore.findBySelector,
                [vm.selectionOptions])
            .then(r => r.data);

        const appPromise = serviceBroker
            .loadViewData(
                CORE_API.ApplicationStore.findBySelector,
                [vm.selectionOptions])
            .then(r => r.data);

        $q.all([attestationInstancePromise, appPromise])
            .then(([attestationInstances, applications]) => {
                vm.applications = applications;
                const instancesByKind = _.groupBy(attestationInstances, d => d.attestedEntityKind);
                vm.gridDataByLogicalFlow = mkAttestationSummaryDataForApps(applications, instancesByKind[entity.LOGICAL_DATA_FLOW.key], displayNameService);
                vm.gridDataByPhysicalFlow = mkAttestationSummaryDataForApps(applications, instancesByKind[entity.PHYSICAL_FLOW.key], displayNameService);

                vm.summaryData = {
                    logical: prepareSummaryData(vm.gridDataByLogicalFlow, vm.selectedYear),
                    physical: prepareSummaryData(vm.gridDataByPhysicalFlow, vm.selectedYear)
                };
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

        vm.config =  {
            logical: Object.assign({}, attestationPieConfig, { onSelect: onSelectLogicalFlowSegment }),
            physical: Object.assign({}, attestationPieConfig, { onSelect: onSelectPhysicalFlowSegment }),
        };

        loadData();
    };


    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadData();
        }
    };


    // -- INTERACT ----
    function updateGridData() {
        const segment = vm.selectedSegment;
        const year = vm.selectedYear;
        const lifecycle = vm.selectedLifecycle;
        const gridData = vm.rawGridData;
        const attestationType = vm.selectedAttestationType;

        vm.extractUrl = mkExtractUrl(attestationType, segment, year, lifecycle);
        vm.gridDataToDisplay = calcGridData(segment, gridData, year, lifecycle);
        vm.visibility.tableView = true;
    }


    vm.onChangeYear = (year) => {
        vm.selectedYear = Number(year);
        loadData();
        updateGridData();
    };

    vm.onChangeLifecycle = (lifecycle) => {
        vm.selectedLifecycle = Number(lifecycle) ? 0 : lifecycle;
        loadData();
        updateGridData();
    };


    function onSelectLogicalFlowSegment(segment) {
        vm.rawGridData = vm.gridDataByLogicalFlow;
        vm.selectedSegment = segment;
        vm.selectedAttestationType = "LOGICAL_DATA_FLOW";
        updateGridData();
    }


    function onSelectPhysicalFlowSegment(segment) {
        vm.rawGridData = vm.gridDataByPhysicalFlow;
        vm.selectedSegment = segment;
        vm.selectedAttestationType = "PHYSICAL_FLOW";
        updateGridData();
    }
}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "DisplayNameService"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzAttestationSummarySection",
    controllerAs: "$ctrl"
};