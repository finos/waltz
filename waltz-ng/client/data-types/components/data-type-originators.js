/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from 'lodash';
import {initialiseData} from '../../common'


const bindings = {
    flowOriginators: '<'
};


const template = require('./data-type-originators.html');


const initialState = {
    dataTypes: [],
    flowOriginators: {}
};


function prepareData(dataTypes = [], flowOriginators = {}) {
    const typesById = _.keyBy(dataTypes, 'id');

    const typeToAppIdsForFlows = _.mapValues(flowOriginators, xs => _.map(xs, 'id'));


    // TODO: enrich with dtu flow info
    return _.map(flowOriginators, (apps, typeId) => {
        return {
            dataType: typesById[typeId],
            appReferences: _.sortBy(apps, 'name')
        };
    });


}


function controller(dataTypeService) {

    const vm = initialiseData(this, initialState);

    dataTypeService.loadDataTypes()
        .then(dataTypes => vm.dataTypes = dataTypes);


    vm.$onChanges = () => {
        vm.originators = prepareData(vm.dataTypes, vm.flowOriginators);
    };

}


controller.$inject = [
    'DataTypeService'
];


const component = {
    bindings,
    template,
    controller
};




export default component;
