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
import {mkSummaryTableHeadings} from "../utilities";
import template from './entity-statistic-detail-panel.html';


const bindings = {
    applications: '<',
    definition: '<',
    orgUnits: '<',
    summary: '<',
    values: '<'
};


const PIE_SIZE = 140;


function mkStatChartData(counts = [], onSelect) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            labelProvider: d => d.key,
            onSelect,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({ key: c.id, count: c.count }))
            .value()
    };
}


function controller() {
    const vm = this;

    const pieClickHandler = d => {
        vm.selectedPieSegment = d;
    };

    vm.$onChanges = () => {
        vm.pie = mkStatChartData(
            vm.summary ? vm.summary.tallies : [],
            pieClickHandler);

        vm.tableHeadings = mkSummaryTableHeadings(vm.definition);
        vm.lastUpdatedAt = vm.summary ? vm.summary.lastUpdatedAt : '';
    }
}


const component = {
    bindings,
    controller,
    template
};


export default component;