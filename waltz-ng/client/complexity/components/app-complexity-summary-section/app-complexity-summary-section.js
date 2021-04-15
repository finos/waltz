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
import _ from "lodash";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from "./app-complexity-summary-section.html";
import {enrichComplexitiesWithKind, findDefaultComplexityKind} from "../../services/complexity-utilities";


const bindings = {
    parentEntityRef: "<",
    targetEntityKind: "<?"
};

const initialState = {
    targetEntityKind: 'APPLICATION',
    selectedKind: null,
    complexities: [],
    complexityKinds: [],
    summarySelection: null,
    visibility: {
        selectKind: false,
        allComplexities: false,
        loading: false
    }
};


const complexityColumnDefs = [
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
        field: 'complexityKind.name',
        displayName: 'Kind',
        width: '20%',
        cellTemplate:`
            <div class="ui-grid-cell-contents">
                 <span ng-bind="row.entity.complexityKind.name"
                 uib-popover="{{row.entity.complexityKind.description}}"
                 popover-append-to-body="true"
                 popover-placement="top"
                 popover-popup-delay="300"
                 popover-trigger="mouseenter">
                </span>
            </div>`
    },{
        field: 'score',
        displayName: 'Score',
        width: '20%',
        headerCellClass: 'waltz-grid-header-right',
        cellTemplate:`
            <div class="ui-grid-cell-contents"
            style="padding-right: 2em">
                 <span class="pull-right"
                       ng-bind="COL_FIELD">
                </span>
            </div>`
    },{
        field: 'provenance',
        displayName: 'Provenance',
        width: '30%'
    }];


function controller($q, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function loadTopComplexityScores() {
        if (_.isNil(vm.selectedKind)) {
            // Returning empty array of scores as we have no selected complexity kind
            return Promise.resolve([]);
        }
        return serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findByTopComplexitiesSummaryByTargetKindAndSelector,
                [
                    vm.selectedKind.id,
                    vm.targetEntityKind,
                    vm.selector
                ])
            .then(r => vm.complexitySummary = r.data);
    }

    function loadComplexityKinds() {
        return serviceBroker
            .loadViewData(
                CORE_API.ComplexityKindStore.findBySelector,
                [vm.targetEntityKind, vm.selector])
            .then(r => {
                vm.complexityKinds = r.data;
                return vm.selectedKind = findDefaultComplexityKind(vm.complexityKinds);
            });
    }

    vm.$onInit = () => {
        vm.complexityColumnDefs = complexityColumnDefs;
        vm.selector = mkSelectionOptions(vm.parentEntityRef);
        loadComplexityKinds()
            .then(loadTopComplexityScores);
    };

    vm.$onChanges = (changes) => {
        if(vm.selector && vm.selectedKind){
            loadTopComplexityScores();
        }
    };

    vm.onSelect = (d) => {
        console.log(d);
        vm.selectedEntity = d;
    };

    vm.refresh = () => {
        vm.visibility.selectKind = false;
        loadTopComplexityScores();
    };

    vm.showAllComplexities = () => {
        vm.visibility.allComplexities = !vm.visibility.allComplexities;
        vm.visibility.loading = true;
        serviceBroker
            .loadViewData(
                CORE_API.ComplexityStore.findBySelector,
                [vm.targetEntityKind, vm.selector])
            .then(r => {
                const enrichedComplexities =  enrichComplexitiesWithKind(r.data, vm.complexityKinds);
                vm.allComplexities = _.orderBy(enrichedComplexities, ['entityReference.name', 'score']);
                vm.visibility.loading = false;
            });
    };

}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


export const component = {
    template,
    bindings,
    controller
};

export const id = "waltzAppComplexitySummarySection";

