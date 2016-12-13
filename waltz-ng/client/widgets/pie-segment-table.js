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

import _ from "lodash";
import {variableScale} from "../common/colors";


const bindings = {
    config: '<',
    data: '<',
    headings: '<',
    selectedSegmentKey: '<'
};


const initialState = {
    total: 0,
    headings: []
};


const defaultConfig = {
    labelProvider: (d) => d.key,
    colorProvider: (d) => variableScale(d)
};


const defaultOnSelect = (d) => console.log('no pie-segment-table::on-select handler provided:', d);


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        if (changes.data) {
            vm.total = _.sumBy(vm.data, 'count');
        }

        if (changes.config) {
            vm.config = _.defaultsDeep(vm.config, defaultConfig);
            vm.rowSelected = (d, e) => {
                e.stopPropagation();
                (vm.config.onSelect || defaultOnSelect)(d);
            };
        }
    };

    vm.asPercentage = (d) => {
        const numerator = d.count || 0;
        const denominator = vm.total || 0;

        return denominator === 0
            ? '-'
            : Number((numerator / denominator) * 100).toFixed(1);
    };
}


const component = {
    bindings,
    controller,
    template: require('./pie-segment-table.html')
};


export default component;