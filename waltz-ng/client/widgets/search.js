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
    clear: '=',
    search: '='
};


function controller() {

    const vm = this;

    vm.doSearch = (q) => {
        if (_.isFunction(vm.search)) vm.search(q);
    };


    vm.clearSearch = () => {
        if (_.isFunction(vm.clear)) vm.clear();
        vm.qry = '';
    };
}


controller.$inject = [];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./search.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
