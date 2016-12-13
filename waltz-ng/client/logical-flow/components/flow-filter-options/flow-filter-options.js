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


const bindings = {
    visible: '<',
    onChange: '<',
    types: '<' // [ dataTypeId... ]
};


const initialState = {
    selectedType: 'ALL',
    selectedScope: 'ALL',
    visible: false,
    onChange: () => console.log('No change handler registered for flow-filter-options-overlay::onChange')
};


function controller() {
    const vm = _.defaults(this, initialState);

    vm.$onChanges = vm.notifyChanges;

    vm.notifyChanges = () => {
        const options = {
            type: vm.selectedType || 'ALL',
            scope: vm.selectedScope || 'ALL'
        };
        vm.onChange(options);
    };

    // -- BOOT ---

    vm.notifyChanges();
}


controller.$inject = [];


const component = {
    controller,
    bindings,
    template: require('./flow-filter-options.html')
};


export default component;