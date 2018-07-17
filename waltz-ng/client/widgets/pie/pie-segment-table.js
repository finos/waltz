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
import {numberFormatter, toPercentage} from "../../common/string-utils";
import template from './pie-segment-table.html';


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
    colorProvider: (d) => variableScale(d),
    descriptionProvider: (d) => null
};


const defaultOnSelect = (d) => console.log('no pie-segment-table::on-select handler provided:', d);


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        if (changes.data) {
            vm.total = _.sumBy(vm.data, 'count');
            vm.totalStr = numberFormatter(vm.total, 2, false);
        }

        if (changes.config) {
            vm.config = _.defaultsDeep(vm.config, defaultConfig);
            vm.rowSelected = (d, e) => {
                e.stopPropagation();
                (vm.config.onSelect || defaultOnSelect)(d);
            };
        }
    };

    vm.asPercentage = (d) => toPercentage(d.count, vm.total);
}


const component = {
    bindings,
    controller,
    template
};


export default component;