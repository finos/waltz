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

import attestationInstanceListUserView from './attestation-instance-list-user-view';
import attestationRunCreate from './attestation-run-create';
import attestationRunList from './attestation-run-list';
import attestationRunView from './attestation-run-view';

const baseState = {
    url: 'attestation'
};

const instanceBaseState = {
    url: '/instance'
};

const instanceUserState = {
    url: '/user',
    views: {'content@': attestationInstanceListUserView}
};

const runBaseState = {
    url: '/run'
};

const runCreateState = {
    url: '/create',
    views: {'content@': attestationRunCreate}
};

const runListState = {
    url: '/list',
    views: {'content@': attestationRunList}
};

const runViewState = {
    url: '/{id:int}',
    views: {'content@': attestationRunView}
};


function setup($stateProvider) {
    $stateProvider
        .state('main.attestation', baseState)
        .state('main.attestation.instance', instanceBaseState)
        .state('main.attestation.instance.user', instanceUserState)
        .state('main.attestation.run', runBaseState)
        .state('main.attestation.run.create', runCreateState)
        .state('main.attestation.run.list', runListState)
        .state('main.attestation.run.view', runViewState);
}


setup.$inject = ['$stateProvider'];


export default setup;