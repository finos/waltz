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

const baseState = {
    url: 'attestation'
};


const runBaseState = {
    url: '/run'
};


const runCreateState = {
    url: '/create',
    views: {'content@': require('./attestation-run-create')}
};


// const runViewState = {
//     url: '/{id:int}',
//     views: {'content@': require('./attestation-run-view')}
// };

function setup($stateProvider) {
    $stateProvider
        .state('main.attestation', baseState)
        .state('main.attestation.run', runBaseState)
        .state('main.attestation.run.create', runCreateState);
        // .state('main.survey.run.view', runViewState)


}


setup.$inject = ['$stateProvider'];


export default setup;