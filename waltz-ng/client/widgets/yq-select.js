
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

import _ from 'lodash';
import template from './yq-select.html';


function controller() {
    const vm = this;

    const currentYear = new Date().getFullYear();
    const { start = { year: currentYear, quarter: 1 } } = vm;
    const { end = { year: start.year + 5, quarter: 4 } } = vm;

    vm.years = _.range(start.year, end.year + 1);
    vm.select = (yq) => {
        if (vm.onSelect()) {
            vm.onSelect()(yq);
        }
    };

    vm.isSelected = (y, q) => vm.selected && vm.selected.year === y && vm.selected.quarter === q;

    vm.isDisabled = (y, q) =>
            (y * 10 + q) < (start.year * 10 + start.quarter) ||
            (y * 10 + q) > (end.year * 10 + end.quarter);
}

export default function() {
    return {
        restrict: 'E',
        replace: true,
        template,
        scope: {
            start: '=',
            end: '=',
            selected: '=',
            onSelect: '&'
        },
        bindToController: true,
        controllerAs: 'ctrl',
        controller
    };
}
