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

import {initialiseData} from "../../common";
import template from "./change-breakdown-table.html";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../common/selector-utils";

const bindings = {
    parentEntityRef: "<",
    selectedDate: "<?",
};


const initialState = {
    targetKind: "APPLICATION",
    visibility:{
        loading: false
    }
};


function controller(serviceBroker, $q) {

    const vm = initialiseData(this, initialState);

    vm.columnDefs = [
        {
            field: "ref",
            name: "Entity",
            cellTemplate:`
            <div class="ui-grid-cell-contents">
                <waltz-entity-link entity-ref="COL_FIELD"
                </waltz-entity-link>
            </div>`
        },{
            field: "childKind",
            name: "Change Type",
            cellFilter: "toDisplayName:'entity'"
        },{
            field: "count",
            name: "Count"
        }];


    function loadChangeSummaries(opts) {
        vm.visibility.loading = true;
        serviceBroker
            .loadViewData(CORE_API.ChangeLogSummariesStore.findSummariesForKindBySelector,
                ["APPLICATION", opts, vm.selectedDate])
            .then(r => {
                vm.data = r.data;
                vm.total = _.sumBy(vm.data, "count");
                vm.visibility.loading = false;
            })
    }

    vm.$onInit = () => {
    };

    vm.$onChanges = (c) => {
        if(c.selectedDate && vm.selectedDate != null){
            const opts = mkSelectionOptions(vm.parentEntityRef);
            loadChangeSummaries(opts)
        } else if (vm.selectedDate == null){
            vm.data = null;
        }
    };

    vm.clearSelectedDate = () => {
        vm.selectedDate = null;
    }
}


controller.$inject = [
    "ServiceBroker",
    "$q"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzChangeBreakdownTable",
    component
}
