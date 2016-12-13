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
    catalog: '='
};

const defaultCatalog = {
    usages: [],
    packages: []
};

function controller($scope) {

    const vm = this;

    $scope.$watch(
        'ctrl.catalog',
        (catalog = defaultCatalog) => {
            const usagesByPkgId = _.countBy(catalog.usages, usage => usage.softwarePackageId);
            vm.countUsages = (pkg) => {
                const count = usagesByPkgId[pkg.id]
                return count > 1
                        ? 'x ' + count
                        : '';
            };

        }
    );



}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {},
    template: require('./simple-software-usage-list.html'),
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
