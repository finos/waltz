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

const bindings = {
    data: '<',
    config: '<',
    title: '@',
    subTitle: '@',
    icon: '@',
    selectedSegmentKey: '<'
};

const MAX_PIE_SEGMENTS = 5;

function controller() {

    const vm = this;

    const defaultOnSelect = (d) => {
        vm.selectedSegmentKey = d
            ? d.key
            : null;
    };

    const dataChanged = (data = []) => {
        vm.total = _.sumBy(data, 'count');

        if (data.length > MAX_PIE_SEGMENTS) {
            const sorted = _.sortBy(data, d => d.count * -1);

            const topData = _.take(sorted, MAX_PIE_SEGMENTS);
            const otherData = _.drop(sorted, MAX_PIE_SEGMENTS);
            const otherDatum = {
                key: 'Other',
                count : _.sumBy(otherData, "count")
            };

            vm.pieData = _.concat(topData, otherDatum);
        } else {
            vm.pieData = data;
        }

    };

    vm.$onChanges = (changes) => {
        dataChanged(vm.data);

        if (changes.config) {
            vm.config = Object.assign(
                {},
                { onSelect: defaultOnSelect },
                vm.config);
        }
    };

    vm.toDisplayName = (k) => vm.config.labelProvider
        ? vm.config.labelProvider(k)
        : k;
}



const component = {
    template: require('./pie-table.html'),
    bindings,
    controller
};


export default component;
