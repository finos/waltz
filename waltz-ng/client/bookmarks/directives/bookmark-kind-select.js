/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import {bookmarkNames} from "../../common/services/display-names.js";
import {bookmarkIconNames} from "../../common/services/icon-names.js";


function createKinds() {
    return _.chain(_.keys(bookmarkNames))
        .union(_.keys(bookmarkIconNames))
        .sortBy()
        .map(k => ({
            code: k,
            name: bookmarkNames[k] || '?',
            icon: bookmarkIconNames[k] || 'square-o'
        }))
        .value();
}

function controller($scope) {

    const kinds = createKinds();

    $scope.$watch('ctrl.value', (nv) => {
        if (nv) {
            const selection = _.find(kinds, { code: nv });
            this.selected = selection;
        }
    });

    this.onChange = (kind) => {
        if (!this.onSelect()) {
            console.warn('bookmark-kind-select: No handler for change notification is registered.  Please provide an on-select callback')
            return;
        }
        this.onSelect()(kind.code, kind);
    }

    this.kinds = kinds;


}

controller.$inject = ['$scope'];

export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        value: '@',
        onSelect: '&'
    },
    template: require('./bookmark-kind-select.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true
});
