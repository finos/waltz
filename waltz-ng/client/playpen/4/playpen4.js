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
import {CORE_API} from '../../common/services/core-api-utils';
import {refToString} from '../../common/entity-utils';

const initialState = {
    // parentEntityRef: { kind: 'APPLICATION', id: /*25747*/ 25991 },
    fooData: []

};



function controller(serviceBroker, $stateParams) {

    const vm = Object.assign(this, initialState);

    vm.$onInit = () => {
        vm.parentEntityRef = { kind: $stateParams.kind, id: $stateParams.id };
        const ref = vm.parentEntityRef;
        const selector = {
            entityReference: ref,
            scope: 'EXACT'
        };

        // vm.addData();
        // vm.addData();
        // vm.addNoData();
        // vm.addData();
        // vm.addData();

        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findByEntityReference,
                [ref]);

        serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowDecoratorStore.findBySelectorAndKind,
                [selector, 'DATA_TYPE']);
    };


    vm.addData = () => {
        const more = {
            DISCOURAGED: _.random(0, 30),
            SECONDARY: _.random(0, 30),
            PRIMARY: _.random(0, 30),
            NO_OPINION: _.random(0, 30)
        };
        vm.fooData = _.concat(vm.fooData, [more])
    };

    vm.addNoData = () => {
        const more = {
            // DISCOURAGED: 0,
            // SECONDARY: 0,
            // PRIMARY: 0,
            // NO_OPINION: 0
        };
        vm.fooData = _.concat(vm.fooData, [more])
    };
}


controller.$inject = [
    'ServiceBroker',
    '$stateParams'
];


const view = {
    template: require('./playpen4.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;
