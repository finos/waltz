/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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
import { initialiseData } from '../../common';
import { invokeFunction } from '../../common/index';

import template from './column-mapper.html';

const bindings = {
    sourceColumns: '<',
    targetColumns: '<',
    existingMappings: '<',
    onChange: '<'
};


const initialState = {
    sourceColumns: [],
    targetColumns: [],
    availableTargetColumns: [],
    mappings: {},
    onChange: (event) => console.log('default onChange handler for column-mapper: ', event)
};


function controller() {
    const vm = initialiseData(this, initialState);

    const mkAvailableTargetColumns = () => {
        const mappedTargets = _.values(vm.mappings);
        return _.differenceBy(vm.targetColumns, mappedTargets, 'name');
    };

    const isComplete = () => {
        const requiredTargets = _.filter(vm.targetColumns, tc => tc.required);
        const mappedTargets = _.values(vm.mappings);
        return _.isEmpty(_.differenceBy(requiredTargets, mappedTargets, 'name'));
    };

    vm.$onInit = () => {
        vm.availableTargetColumns = vm.targetColumns;
    };

    vm.$onChanges = (changes) => {
        if(changes.existingMappings) {
            vm.mappings = vm.existingMappings || {};
            vm.onMappingSelect();
        }
    };

    vm.onMappingSelect = () => {
        vm.availableTargetColumns = mkAvailableTargetColumns();
        const event = {
            mappings: _.omitBy(vm.mappings, (v,k) => _.isEmpty(v)),
            isComplete
        };
        invokeFunction(vm.onChange, event);
    };

    vm.resetMappings = () => {
        vm.mappings = {};
        vm.onMappingSelect();
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzColumnMapper'
};
