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

import _ from 'lodash';

import {initialiseData} from '../../../common'
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './data-type-originators.html';
import {mkSelectionOptions} from "../../../common/selector-utils";


const bindings = {
    parentEntityRef: '<',
    flowOriginators: '<'
};


const initialState = {
    dataTypes: [],
    flowOriginators: {}
};


function prepareData(dataTypes = [], flowOriginators = {}) {
    const typesById = _.keyBy(dataTypes, 'id');

    // TODO: enrich with dtu flow info
    return _.map(flowOriginators, (apps, typeId) => {
        return {
            dataType: typesById[typeId],
            appReferences: _.sortBy(apps, 'name')
        };
    });


}


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const dtPromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);

        const originatorPromise = serviceBroker
            .loadViewData(
                CORE_API.DataTypeUsageStore.findForUsageKindByDataTypeIdSelector,
                [ 'ORIGINATOR', mkSelectionOptions(vm.parentEntityRef) ])
            .then(r => vm.flowOriginators = r.data);

        dtPromise
            .then(() => originatorPromise)
            .then(() => vm.originators = prepareData(vm.dataTypes, vm.flowOriginators));
    };

    vm.$onChanges = () => {
        vm.originators = prepareData(vm.dataTypes, vm.flowOriginators);
    };

}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


const id = 'waltzDataTypeOriginators';


export default {
    component,
    id
};
