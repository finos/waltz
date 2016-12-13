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

import _ from 'lodash';
import {initialiseData} from '../../../common';


const bindings = {
    apps: '<',
    endUserApps: '<',
    sourceDataRatings: '<',
};


const initialState = {
    apps: [],
    endUserApps: [],
    sourceDataRatings: [],
};


const template = require('./apps-section.html');


function combine(apps = [], endUserApps = []) {
    return _.concat(apps, endUserApps);
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.combinedApps = combine(vm.apps, vm.endUserApps);
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn(`apps.csv`);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;