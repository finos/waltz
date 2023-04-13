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
import {initialiseData} from "../../../common";

import template from "./entity-costs-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import * as _ from "lodash";
import namedSettings from "../../../system/named-settings"


const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    exportAllowed: true
};


function mkColumnDefs(uiGridConstants){
    return [
        {
            field: 'costKind.name',
            displayName: 'Kind',
            width: '25%',
            cellTemplate:`
            <div class="ui-grid-cell-contents">
                 <span ng-bind="row.entity.costKind.name"
                       uib-popover="{{row.entity.costKind.description}}"
                       popover-append-to-body="true"
                       popover-placement="top"
                       popover-popup-delay="300"
                       popover-trigger="mouseenter">
                </span>
            </div>`
        },{
            field: 'year',
            displayName: 'Year',
            width: '25%',
            sort: {
                direction: uiGridConstants.DESC,
                priority: 0,
            },
        },{
            field: 'amount',
            displayName: 'Amount',
            width: '25%',
            headerCellClass: 'waltz-grid-header-right',
            cellTemplate:`
            <div class="ui-grid-cell-contents"
                 style="padding-right: 2em">
                 <span class="pull-right">
                    <waltz-currency-amount amount="COL_FIELD">
                    </waltz-currency-amount>
                </span>
            </div>`
        },{
            field: 'provenance',
            displayName: 'Provenance',
            width: '25%'
        }];
}


function controller($q, serviceBroker, uiGridConstants, settingsService) {

    const vm = initialiseData(this, initialState);

    function loadCostInfo() {
        const costKindPromise = serviceBroker
            .loadAppData(CORE_API.CostKindStore.findAll)
            .then(r => _.map(r.data, d => d.costKind));

        const costPromise = serviceBroker
            .loadViewData(CORE_API.CostStore.findByEntityReference, [vm.parentEntityRef])
            .then(r => r.data);

        $q
            .all([costKindPromise, costPromise])
            .then(([costKinds, costs]) => {

                const costKindsById = _.keyBy(costKinds, d => d.id);

                vm.costInfo = _.map(costs, d => Object.assign(
                    {},
                    d,
                    {costKind: _.get(costKindsById, d.costKindId, "Unknown")}));

                const noDefault = _.isUndefined(_.find(vm.costInfo, d => d.costKind.isDefault));

                vm.displayCost = _
                    .chain(vm.costInfo)
                    .filter(d => noDefault || d.costKind.isDefault)
                    .maxBy(d => d.year)
                    .value();

                vm.yearlyCostInfo = _.chain(vm.costInfo)
                    .filter(d => !d.costKind.isDefault)
                    .groupBy(d => d.costKindId)
                    .flatMap((costs, k) => _.maxBy(costs, c => c.year))
                    .orderBy(d => d.year, "desc")
                    .value();
            });
    }

    function allowCostExport() {
        settingsService
            .findOrDefault(namedSettings.costExportEnabled, true)
            .then(exportAllowed => {
                vm.exportAllowed = !(exportAllowed === 'false');
            });
    }

    vm.$onInit = () => {
        allowCostExport();
        loadCostInfo();
        vm.entityCostColumnDefs = mkColumnDefs(uiGridConstants);

    };

    vm.$onChanges = (changes) => {
        loadCostInfo();
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "uiGridConstants",
    "SettingsService"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEntityCostsSection"
};
