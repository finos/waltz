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

import {toDomain} from "../common/string-utils";
import template from './external-link.html';

const BINDINGS = {
    url: '@',
    title: '@',
    showUrl: '='
};


function toPrettyUrl(url = "") {
    return _.truncate( toDomain(url), { length: 60 });
}


function controller($scope) {

    const vm = this;

    $scope.$watchGroup(
        ['ctrl.url', 'ctrl.title', 'ctrl.showUrl'],
        ([url, title, showUrl = false]) => {
            vm.prettyTitle = title
                ? title
                : toPrettyUrl(url);
            vm.prettyUrl = toPrettyUrl(url);
            vm.showAside = (showUrl && title && url);
        });

}


controller.$inject = [
    '$scope'
];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template
});

