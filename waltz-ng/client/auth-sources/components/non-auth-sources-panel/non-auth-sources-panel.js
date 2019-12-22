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
import {CORE_API} from "../../../common/services/core-api-utils";
import template from './non-auth-sources-panel.html';


const bindings = {
    nonAuthSources: '<'
};


const initialState = {
    groupedNonAuthSources: [],
    selected: null,
    totalCount: 0
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        if (! vm.dataTypes) return;

        const dataTypesById = _.keyBy(vm.dataTypes, 'id');

        vm.groupedNonAuthSources = _
            .chain(vm.nonAuthSources)
            .groupBy('dataTypeId')
            .map((v, k) => {
                return {
                    dataType: dataTypesById[k],
                    sources: _.sortBy(v, 'sourceReference.name')
                };
            })
            .sortBy('dataType.name')
            .value();

        vm.totalCount = _.sumBy(vm.nonAuthSources, 'count');
    };


    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data)
            .then(refresh);
    };

    vm.$onChanges = refresh;

    vm.showDetail = selected =>
        vm.selected = selected;
}


controller.$inject = ['ServiceBroker'];


export const component = {
    bindings,
    controller,
    template
};


export const id = "waltzNonAuthSourcesPanel";
