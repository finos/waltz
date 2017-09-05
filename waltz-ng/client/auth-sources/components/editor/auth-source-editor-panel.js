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

import template from './auth-source-editor-panel.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from 'lodash';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {};


function controller($q, serviceBroker, notification) {

    const vm = initialiseData(this, initialState);
    global.vm = vm;

    const refresh = () => {
        serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findByReference, [vm.parentEntityRef], { force: true })
            .then(r => {

                const orgUnitsById = _.keyBy(vm.orgUnits, 'id');
                const dataTypesByCode = _.keyBy(vm.dataTypes, 'code');
                const dataTypesById = _.keyBy(vm.dataTypes, 'id');
                const authSources = r.data;

                vm.authSourceTable = _
                    .chain(authSources)
                    .map(d => {
                        return {
                            authSource: d,
                            orgUnit: orgUnitsById[d.parentReference.id],
                            dataType: dataTypesByCode[d.dataType]
                        };
                    })
                    .sortBy('dataType.name', 'authSource.applicationReference.name')
                    .value();
            });
    };

    vm.$onInit = () => {
        console.log('go', vm)
        const promises  = [
            serviceBroker
                .loadAppData(CORE_API.DataTypeStore.findAll)
                .then(r => vm.dataTypes = r.data),
            serviceBroker
                .loadAppData(CORE_API.OrgUnitStore.findAll)
                .then(r => vm.orgUnits = r.data)
        ];

        $q.all(promises).then(() => refresh());
    };

    vm.remove = (authSource) => {
        if (confirm('Are you sure you want to delete this Authoritative Source ?')) {
            serviceBroker
                .execute(CORE_API.AuthSourcesStore.remove, [authSource.id])
                .then(() => {
                    refresh();
                    notification.warning('Authoritative Source removed');
                });
        }
    }
}


controller.$inject = [
    '$q',
    'ServiceBroker',
    'Notification'
];


export const component = {
    template,
    controller,
    bindings
};


export const id = 'waltzAuthSourceEditorPanel';