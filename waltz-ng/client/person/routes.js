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
import HomePage from './pages/home/person-home';
import PersonPage from './pages/view/person-view';
import {CORE_API} from "../common/services/core-api-utils";


function resolvePersonByEmpId($stateParams, serviceBroker) {
    return serviceBroker
        .loadViewData(
            CORE_API.PersonStore.getByEmployeeId,
            [ $stateParams.empId ])
        .then(r => r.data);
}

resolvePersonByEmpId.$inject = [
    '$stateParams',
    'ServiceBroker'
];

function resolvePersonById($stateParams, serviceBroker) {
    return serviceBroker
        .loadViewData(
            CORE_API.PersonStore.getById,
            [ $stateParams.id ])
        .then(r => r.data);
}

resolvePersonById.$inject = [
    '$stateParams',
    'ServiceBroker'
];


// --- ROUTES ---

const personHome = {
    url: 'person',
    views: {'content@': HomePage }
};

const personView = {
    url: '/:empId',
    views: {'content@': PersonPage },
    resolve: {
        person: resolvePersonByEmpId
    }
};

const personViewByPersonId = {
    url: '/id/:id',
    views: {'content@': PersonPage },
    resolve: {
        person: resolvePersonById
    }
};


// --- SETUP ---

function setup($stateProvider) {

    $stateProvider
        .state('main.person', personHome)
        .state('main.person.view', personView)
        .state('main.person.id', personViewByPersonId);
}

setup.$inject = ['$stateProvider'];


export default setup;