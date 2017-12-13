/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
