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

import template from "./app-costs-summary-section.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import * as _ from "lodash";
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: "<",
    targetEntityKind: "<?"
};

const initialState = {
    targetEntityKind: 'APPLICATION',
    selectedKind: null,
    costInfo: [],
    visibility: {
        selectKind: false,
        allCosts: false,
        loading: false
    },
    selectedEntity: null
};


function mkColumnDefs(uiGridConstants){
    return [
        {
            field: 'entityReference.name',
            displayName: 'Name',
            width: '30%',
            cellTemplate:`
            <div class="ui-grid-cell-contents">
                 <waltz-entity-link entity-ref="row.entity.entityReference" 
                                    icon-placement="right">
                 </waltz-entity-link>
            </div>`
        },{
            field: 'costKind.name',
            displayName: 'Kind',
            width: '20%',
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
            width: '10%',
            sort: {
                direction: uiGridConstants.DESC,
                priority: 0,
            },
        },{
            field: 'amount',
            displayName: 'Amount',
            width: '20%',
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
            width: '20%'
        }];
}


function controller($q, serviceBroker, uiGridConstants) {

    const vm = initialiseData(this, initialState);

    function loadCostKinds() {
        return serviceBroker
            .loadAppData(CORE_API.CostKindStore.findExistingBySelector,
                [vm.targetEntityKind, vm.selector])
            .then(r => {
                vm.costKinds = r.data;
                vm.costKindsById = _.keyBy(vm.costKinds, d => d.id);
                vm.selectedKind = _.find(vm.costKinds, d => d.isDefault)
            });
    }

    function loadTopCostsByIdAndSelector(){
        return serviceBroker
            .loadViewData(CORE_API.CostStore.findByCostKindAndSelector,
                [vm.selectedKind.id, vm.targetEntityKind, vm.selector],
                { force: true })
            .then(r => vm.topCosts = r.data);
    }

    vm.loadAllCosts = () => {
        vm.visibility.loading = true;
        serviceBroker
            .loadViewData(CORE_API.CostStore.findBySelector,
                [vm.targetEntityKind, mkSelectionOptions(vm.parentEntityRef)])
            .then(r => {
                vm.costInfo = _.map(r.data,
                    d => Object.assign(
                        {},
                        d,
                        {costKind: _.get(vm.costKindsById, d.costKindId, 'Unknown')}));
                vm.visibility.loading = false;
            });
    };

    vm.$onInit = () => {
        vm.entityCostColumnDefs = mkColumnDefs(uiGridConstants);
        vm.selector = mkSelectionOptions(vm.parentEntityRef);

        loadCostKinds()
            .then(() => loadTopCostsByIdAndSelector());
    };

    vm.$onChanges = () => {
        if (vm.selector){
            loadCostKinds()
                .then(() => loadTopCostsByIdAndSelector())
        }
    };

    vm.refresh = () => {
        vm.visibility.selectKind = false;
        loadTopCostsByIdAndSelector();
    };

    vm.showAllCosts = () =>  {
        vm.loadAllCosts();
        vm.visibility.allCosts = !vm.visibility.allCosts;
    };

    vm.onSelect = (d) => {

        if (vm.selectedEntity && vm.selectedEntity.entityReference.id === d.entityReference.id){
            vm.onClearSelectedEntity();
        } else {
            vm.selectedEntity = d;
        }
    };

    vm.onClearSelectedEntity = () => {
        vm.selectedEntity = null;
    }
}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "uiGridConstants"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzAppCostsSummarySection"
};
