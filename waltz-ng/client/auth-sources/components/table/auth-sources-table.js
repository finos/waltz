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
import {CORE_API} from "../../../common/services/core-api-utils";
import {ascending} from "d3-array";


const bindings = {
    parentEntityRef: '<',
    authSources: '<',
    orgUnits: '<',
};


const initialState = {
    consumersByAuthSourceId: {},
    visibility: {
        consumerColumn: false
    }
};


const template = require('./auth-sources-table.html');


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    const refresh = () => {
        const dataTypesByCode= _.keyBy(vm.dataTypes, 'code');
        const dataTypesById= _.keyBy(vm.dataTypes, 'id');
        const orgUnitsById = _.keyBy(vm.orgUnits, 'id');

        const authSources = _.map(vm.authSources, d => {
            return {
                app: d.applicationReference,
                dataType: Object.assign({}, dataTypesByCode[d.dataType], { kind: 'DATA_TYPE' }),
                appOrgUnit: d.appOrgUnitReference,
                declaringOrgUnit: Object.assign({}, orgUnitsById[d.parentReference.id], { kind: 'ORG_UNIT' }),
                description: d.description,
                rating: d.rating,
                consumers: vm.consumersByAuthSourceId[d.id] || []
            };
        });

        vm.authSourceTable = nest()
            .key(d => d.dataType.id)
            .sortKeys((a, b) => ascending(dataTypesById[a].name, dataTypesById[b].name))
            .sortValues((a, b) => ascending(a.app.name, b.app.name))
            .entries(authSources);
    };

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data)
            .then(refresh);

        serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnits = r.data)
            .then(refresh);
    };

    vm.$onChanges = () => {
        const isDataTypeEntity = _.get(vm, 'parentEntityRef.kind', null) === 'DATA_TYPE';
        if (isDataTypeEntity) {
            vm.visibility.consumerColumn = true;

            const selector = {
                entityReference: vm.parentEntityRef,
                scope: 'CHILDREN'
            };

            serviceBroker
                .loadViewData(
                    CORE_API.AuthSourcesStore.calculateConsumersForDataTypeIdSelector,
                    [ selector ])
                .then(r => {
                    vm.consumersByAuthSourceId = _
                        .chain(r.data)
                        .keyBy(d => d.key.id)
                        .mapValues(v => _.sortBy(v.value, 'name'))
                        .value();
                    refresh();
                });

        } else {
            refresh();
        }
    }
}


controller.$inject = ['ServiceBroker'];


export const component = {
    bindings,
    controller,
    template
};

export const id = 'waltzAuthSourcesTable';

