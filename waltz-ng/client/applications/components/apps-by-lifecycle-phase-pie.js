
/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import {lifecyclePhaseColorScale} from "../../common/colors";
import {lifecyclePhaseDisplayNames} from '../../common/services/display_names';
import {toKeyCounts} from "../../common";


const bindings = {
    applications: '<',
    size: '<'
};


const template = require('./apps-by-lifecycle-phase-pie.html');


const DEFAULT_SIZE = 80;


const config = {
    colorProvider: (d) => lifecyclePhaseColorScale(d.data.key),
    size: DEFAULT_SIZE,
    labelProvider: d => lifecyclePhaseDisplayNames[d.key] || 'Unknown'
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
