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

import {orgUnitsResolver, appTalliesResolver, endUserAppTalliesResolver} from "./resolvers.js";

import ListView from "./pages/list-view/list-view";
import UnitView from "./pages/unit-view/unit-view";


const baseState = {
    resolve: {
        appTallies: appTalliesResolver,
        endUserAppTallies: endUserAppTalliesResolver,
        orgUnits: orgUnitsResolver
    }
};

const listState = {
    url: 'org-units',
    views: {'content@': ListView}
};


const viewState = {
    url: 'org-units/{id:int}',
    views: {
        'content@': UnitView
    }
};


function setup($stateProvider) {
    $stateProvider
        .state('main.org-unit', baseState)
        .state('main.org-unit.list', listState)
        .state('main.org-unit.view', viewState);
}

setup.$inject = ['$stateProvider'];


export default setup;