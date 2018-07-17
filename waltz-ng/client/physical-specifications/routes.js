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
import View from './physical-specification-view';


const baseState = {
};


const viewState = {
    url: 'physical-specification/{id:int}',
    views: {'content@': View },
};


function setup($stateProvider) {

    $stateProvider
        .state('main.physical-specification', baseState)
        .state('main.physical-specification.view', viewState);
}


setup.$inject = ['$stateProvider'];


export default setup;