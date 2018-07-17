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
import {termSearch} from "../../common";
import template from './software-catalog-section.html';

const BINDINGS = {
    catalog: '='
};


const FIELDS_TO_SEARCH = ['vendor', 'name', 'version', 'maturityStatus'];



function controller($scope) {

    const vm = this;



    $scope.$watchGroup(
        ['ctrl.qry', 'ctrl.catalog.packages'],
        ([qry, packages = []]) => {
            vm.filteredPackages = qry
                ? termSearch(packages, qry, FIELDS_TO_SEARCH)
                : packages;
        }
    );


    $scope.$watch(
        'ctrl.catalog.usages',
        (usages = []) => {
            vm.usages = _.chain(usages)
                .groupBy('provenance')
                .value();
        }
    );

}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        panel: '='
    },
    template,
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
