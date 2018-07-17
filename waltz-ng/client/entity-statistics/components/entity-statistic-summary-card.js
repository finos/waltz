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
import {variableScale} from "../../common/colors";
import {mkSummaryTableHeadings, navigateToStatistic} from "../utilities";
import template from './entity-statistic-summary-card.html';

const bindings = {
    definition: '<',
    parentRef: '<',
    subTitle: '@',
    summary: '<'
};


const PIE_SIZE = 100;


function mkStatChartData(counts = []) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            labelProvider: d => d.key,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({
                key: c.id,
                count: c.count
            }))
            .value()
    };
}


function controller($state) {

    const vm = this;

    vm.$onChanges = () => {
        const tallies = vm.summary
            ? vm.summary.tallies
            : [];
        vm.pie = mkStatChartData(tallies);
        vm.tableHeadings = mkSummaryTableHeadings(vm.definition);
    };

    vm.goToStatistic = (definition) => {
        navigateToStatistic($state, definition.id, vm.parentRef);
    };
}


controller.$inject = [
    '$state'
];


const component = {
    bindings,
    controller,
    template
};


export default component;