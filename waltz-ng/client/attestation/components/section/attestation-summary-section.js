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
import * as _ from "lodash";


const initialState = {
    columnDefs: attestationSummaryColumnDefs,
    visibility : {
        tableView: false
    },
    selectedFlowType: null
};


const bindings = {
    parentEntityRef: "<",
    filters: "<"
};


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
                    logical: prepareSummaryData(vm.gridDataByLogicalFlow),
                    physical: prepareSummaryData(vm.gridDataByPhysicalFlow)
                };
            });
    };

    vm.$onInit = () => {
        vm.config =  {
            logical: Object.assign({}, attestationPieConfig, { onSelect: vm.onSelectLogicalFlow }),
            physical: Object.assign({}, attestationPieConfig, { onSelect: vm.onSelectPhysicalFlow }),
        };

        loadData();
    };

    const gridSelected = (d, grid) => {
        vm.selectedApps = _.filter(grid, app => app.isAttested === d.key);
        vm.exportFlowType = _.find(grid, r => !_.isUndefined(r.attestation)).attestation.attestedEntityKind;
        vm.visibility.tableView = true;
    };

    // -- INTERACT ----

    vm.onSelectLogicalFlow = (d) => {
        gridSelected(d, vm.gridDataByLogicalFlow);
    };

    vm.onSelectPhysicalFlow = (d) => {
        gridSelected(d, vm.gridDataByPhysicalFlow)
    };

    vm.$onChanges = (changes) => {
        if(changes.filters) {
            loadData();
        }
    };


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