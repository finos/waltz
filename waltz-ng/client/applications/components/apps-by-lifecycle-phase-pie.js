
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

import {lifecyclePhaseColorScale} from "../../common/colors";
import {lifecyclePhase} from '../../common/services/enums/lifecycle-phase';
import {toKeyCounts} from "../../common";
import template from './apps-by-lifecycle-phase-pie.html';


const bindings = {
    applications: '<',
    size: '<'
};




const DEFAULT_SIZE = 80;


const config = {
    colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
    size: DEFAULT_SIZE,
    labelProvider: d => lifecyclePhase[d.key] ? lifecyclePhase[d.key].name : 'Unknown'
};


function calcAppPhasePieStats(apps = []) {
    return toKeyCounts(apps, a => a.lifecyclePhase);
}


function controller() {
    const vm = this;

    vm.config = config;
    vm.data = [];

    vm.$onChanges = () => {
        vm.config.size = vm.size
            ? vm.size
            : DEFAULT_SIZE;
        vm.data = calcAppPhasePieStats(vm.applications);
    };
}


const component = {
    template,
    bindings,
    controller
};


export default component;
