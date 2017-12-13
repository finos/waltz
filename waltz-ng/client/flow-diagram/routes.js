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

import FlowDiagramView from "./flow-diagram-view";


const baseState = {
};


const viewState = {
    url: 'flow-diagram/{id:int}',
    views: {
        'content@': FlowDiagramView
    }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.flow-diagram', baseState)
        .state('main.flow-diagram.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;