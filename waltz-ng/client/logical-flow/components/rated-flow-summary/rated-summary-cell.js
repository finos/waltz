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


const BINDINGS = {
    totalCount: '<',
    directCount: '<',
    maxCount: '<',
    decorator: '<',
    rating: '<',
    onClick: '&'
};


const initialState = {
    popoverText: '...',
    onClick: () => console.log("No on-click handler passed to rating-flow-summary-cell")
};


function calcPopoverText(decorator, directCount = 0, totalCount = 0) {
    if (! decorator) return "?";
    return `${decorator.name}.  Direct: ${directCount}, indirect: ${totalCount - directCount} (total: ${totalCount})`;
}


function mkStackValues(direct = 0, total = 0) {
    return [ direct, (total - direct )];
}


function controller() {
    const vm = _.defaultsDeep(this, initialState)

    vm.cellClick = ($event) => {
        vm.onClick();
        $event.stopPropagation();
    };

    vm.$onChanges = () => {
        vm.popoverText = calcPopoverText(vm.decorator, vm.directCount, vm.totalCount);
        vm.stackValues = mkStackValues(vm.directCount, vm.totalCount)
    };
}


const component = {
    bindings: BINDINGS,
    controller,
    template: require('./rated-summary-cell.html')
};


export default component;