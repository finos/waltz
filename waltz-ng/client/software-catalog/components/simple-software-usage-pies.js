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
import {maturityColorScale, variableScale} from "../../common/colors";
import template from './simple-software-usage-pies.html';


const bindings = {
    usages: '<',
    packages: '<'
};


const PIE_SIZE = 70;


function prepareStats(items = [], usages = []) {
    const usageCounts = _.countBy(usages, 'softwarePackageId');

    const countPieDataBy = (items = [], fn = (x => x)) =>
        _.chain(items)
            .groupBy(fn)
            .map((group, key) => {
                const calculatedCount = _.reduce(
                    group,
                    (acc, groupItem) => acc + usageCounts[groupItem.id] || 1,
                    0);
                return {
                    key,
                    count: calculatedCount
                };
            })
            .value();


    return {
        maturity: countPieDataBy(items, item => item.maturityStatus),
        vendor: countPieDataBy(items, item => item.vendor)
    };
}


function controller() {

    const vm = this;

    vm.pieConfig = {
        maturity: {
            size: PIE_SIZE,
            colorProvider: (d) => maturityColorScale(d.data.key)
        },
        vendor: {
            size: PIE_SIZE,
            colorProvider: (d) => variableScale(d.data.key)
        }
    };


    const recalcPieData = () => {
        vm.pieData = prepareStats(vm.packages, vm.usages);
    };


    vm.$onChanges = () => {
        if(vm.packages && vm.usages) {
            recalcPieData();
        }
    }

}


controller.$inject = [ ];


const component = {
    bindings,
    template,
    controller
};


export default component;
