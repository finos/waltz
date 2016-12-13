
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

import {ragColorScale} from "../../common/colors";
import {toKeyCounts} from "../../common";


const bindings = {
    applications: '<',
    size: '<'
};


const template = require('./apps-by-investment-pie.html');


const DEFAULT_SIZE = 80;


const investmentLabels = {
    'R' : 'Disinvest',
    'A' : 'Maintain',
    'G' : 'Invest'
};


const config = {
    colorProvider: (d) => ragColorScale(d.data.key),
    size: DEFAULT_SIZE,
    labelProvider: (d) => investmentLabels[d.key] || 'Unknown'
};


function calcAppInvestmentPieStats(apps = []) {
    return toKeyCounts(apps, a => a.overallRating);
}


function controller() {
    const vm = this;

    vm.config = config;
    vm.data = [];

    vm.$onChanges = () => {
        vm.config.size = vm.size
            ? vm.size
            : DEFAULT_SIZE;
        vm.data = calcAppInvestmentPieStats(vm.applications);
    };
}


const component = {
    template,
    bindings,
    controller
};

export default component;
