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
import UnitView from "./measurable-view";
import ListView from "./measurable-list";


const baseState = {};


const viewState = {
    url: 'measurable/{id:int}',
    views: {
        'content@': UnitView
    }
};


const listState = {
    url: 'measurable?kind',
    views: {
        'content@': ListView
    }
};


function bouncer($state, $stateParams, measurableStore) {
    measurableStore
        .findByExternalId($stateParams.id)
        .then(ms => {
            const m = _.find(ms, {kind: 'CAPABILITY'});
            if (m) {
                $state.go('main.measurable.view', {id: m.id});
            } else {
                console.log(`Cannot find measure corresponding to old capability: ${$stateParams.id}`);
            }
        });
}


bouncer.$inject = [
    '$state',
    '$stateParams',
    'MeasurableStore'
];


const bouncerState = {
    url: 'capabilities/{id:int}',
    resolve: {
        bouncer
    }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.capabilityBouncer', bouncerState)
        .state('main.measurable', baseState)
        .state('main.measurable.list', listState)
        .state('main.measurable.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;