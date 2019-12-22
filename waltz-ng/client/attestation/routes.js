/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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