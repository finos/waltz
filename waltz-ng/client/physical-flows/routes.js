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

const baseState = {
};


const viewState = {
    url: 'physical-flow/{id:int}',
    views: {'content@': require('./physical-flow-view') },
};



const registrationState = {
    url: 'physical-flow/registration/{kind:string}/{id:int}',
    views: {'content@': require('./physical-flow-registration') },
};


function setup($stateProvider) {

    $stateProvider
        .state('main.physical-flow', baseState)
        .state('main.physical-flow.registration', registrationState)
        .state('main.physical-flow.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;