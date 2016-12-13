

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

import ListView from "./list-view";
import CapabilityView from "./capability-view";


const baseState = {
    resolve: {
        capabilities: [
            'CapabilityStore',
            (capabilityStore) => capabilityStore.findAll()
        ]
    }
};


const listState = {
    url: 'capabilities',
    views: {'content@': ListView}
};


const viewState = {
    url: 'capabilities/{id:int}',
    views: { 'content@': CapabilityView }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.capability', baseState)
        .state('main.capability.list', listState)
        .state('main.capability.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;