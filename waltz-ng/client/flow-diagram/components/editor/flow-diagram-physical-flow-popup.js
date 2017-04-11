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

import _ from 'lodash';
import {initialiseData} from '../../../common';
import {toGraphId} from '../../flow-diagram-utils';

/**
 * @name waltz-flow-diagram-add-flow-popup
 *
 * @description
 * This component ...
 */


const bindings = {
    physicalFlows: '<',
    physicalSpecifications: '<',
    existingEntities: '<',
    commandProcessor: '<',
    onDismiss: '<'
};


const initialState = {
    logicalFlows: [],
    existingEntities: [],
    node: null,
    isUpstream: true,
    commandProcessor: () => console.log('wdafp: default command processor'),
    onDismiss: () => console.log('wdafp: default on-dismiss'),
};


const template = require('./flow-diagram-physical-flow-popup.html');


function sameRef(r1, r2) {
    return r1.kind === r2.kind && r1.id === r2.id;
}


function prepareFlows(physicalFlows = [], physicalSpecifications = [], existingEntities = []) {
    const specById = _.keyBy(physicalSpecifications, 'id');
    return _.map(physicalFlows, f => {
        return {
            flow: f,
            specification: specById[f.specificationId]
        }
    })
}


function controller() {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        const description = "Define the physical flows";

        vm.description = description;
        vm.title = `Define Physical Flows`;


        console.log(vm.existingEntities)
        console.log(vm.physicalFlows)
        console.log(vm.physicalSpecifications)

        vm.flows = prepareFlows(vm.physicalFlows, vm.physicalSpecifications, vm.existingEntities);
    };

}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;