/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {nest} from "d3-collection";
import {ascending} from "d3-array";
import {CORE_API} from "../../../common/services/core-api-utils";

const bindings = {
    nonAuthSources: '<'
};


const initialState = {
    nonAuthSourceTable: []
};


const template = require('./non-auth-sources-table.html');


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        if (! vm.dataTypes) return;

        const dataTypesById= _.keyBy(vm.dataTypes, 'id');

        const nonAuthSources = _.map(vm.nonAuthSources, d => {
            return {
                source: d.sourceReference,
                dataType: Object.assign({}, dataTypesById[d.dataTypeId], { kind: 'DATA_TYPE' }),
                count: d.count
            };
        });

        vm.nonAuthSourcesTable = nest()
            .key(d => d.dataType.id)
            .sortKeys((a, b) => ascending(dataTypesById[a].name, dataTypesById[b].name))
            .sortValues((a, b) => ascending(a.source.name, b.source.name))
            .entries(nonAuthSources);
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


export const id = "waltzNonAuthSourcesTable";
