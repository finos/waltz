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

import _ from "lodash";
import {initialiseData, invokeFunction} from "../../common";
import template from './actor-selector.html';


const bindings = {
    allActors: '<',
    onSelect: '<'
};




const initialState = {
    allActors: [],
    actors: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.refresh = function (query) {
        if (!query) return;
        const queryLc = _.lowerCase(query);
        vm.actors = _.filter(vm.allActors, (a) => _.startsWith(_.lowerCase(a.name), queryLc));
    };

    vm.select = (item) => {
        invokeFunction(vm.onSelect, Object.assign(item, {kind: 'ACTOR'}));
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;


