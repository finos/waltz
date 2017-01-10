
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


const BINDINGS = {
    labelOn: '@',
    labelOff: '@',
    iconOn: '@',
    iconOff: '@',
    onToggle: '&',
    state: '<'
};


const initialState = {
    labelOn: '',
    labelOff: '',
    iconOn: 'toggle-on',
    iconOff: 'toggle-off',
    onToggle: () => console.log('no on-toggle handler supplied to waltz-toggle component'),
    state: false
};


function controller() {
    _.defaultsDeep(this, initialState);
}


export default [
    () => ({
        restrict: 'E',
        replace: false,
        template: require('./toggle.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    })
]
;
