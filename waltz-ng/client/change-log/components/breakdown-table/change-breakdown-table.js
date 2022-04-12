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
import template from "./change-breakdown-table.html";
import {refToString} from "../../../common/entity-utils";

const bindings = {
    summaries: "<",
    startDate: "<?",
    endDate: "<?",
    onDismiss: "<?",
    onDetailSelect: "<?"
};


const initialState = {
    visibility:{
        loading: true
    },
    total: 0,
    summaries: [],
    onDismiss: () => console.log("wcbt-onDismiss: default handler"),
    onDetailSelect: (ref, date) => console.log("wcbt-onDetailSelect: default handler", {ref}),
};


function prepareColumnDefs() {
    return [
        {
            field: "ref",
            name: "Entity",
            toSearchTerm: d => d.ref.name,
            cellTemplate:`
                <div class="ui-grid-cell-contents clickable"
                     ng-click="grid.appScope.onRowSelect(row.entity.ref)">
                    <waltz-entity-icon-label entity-ref="COL_FIELD"
                    </waltz-entity-icon-label>
                </div>`
        },{
            field: "counts",
            name: "Types of Changes",
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <ul class="list-inline">
                        <li ng-repeat="c in COL_FIELD">
                            <span class="clickable"
                                  ng-click="grid.appScope.onRowSelect(row.entity.ref)">
                                <span ng-bind="c.kind | toDisplayName:'entity'"></span>
                                <span class="wcbt-count" ng-bind="c.count"></span>
                            </span>
                        </li>
                    </ul>
                </div>`
        }
    ];
}


function calcTotal(summaries = []) {
    return _.sumBy(summaries, "count");
}


function groupData(summaries = []) {
    return _
        .chain(summaries)
        .groupBy(d => refToString(d.ref))
        .map((v, k) => ({
            ref: v[0].ref,
            counts: _.chain(v)
                .map(d => ({count: d.count, kind: d.childKind}))
                .orderBy(d => d.count)
                .value()
        }))
        .orderBy(d => d.ref.name)
        .value();
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.onRowSelect = (ref) => vm.onDetailSelect(ref, vm.startDate, vm.endDate);


    vm.$onInit = () => {
        vm.columnDefs = prepareColumnDefs();
    };


    vm.$onChanges = (c) => {
        if(c.summaries && vm.summaries != null){
            vm.total = calcTotal(vm.summaries);
            vm.groupedData = groupData(vm.summaries);
        }
    };

    vm.clearSelectedDate = () => {
        vm.onDismiss();
    };
}


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzChangeBreakdownTable",
    component
}
