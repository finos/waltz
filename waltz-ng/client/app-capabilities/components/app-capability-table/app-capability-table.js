
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

import _ from 'lodash';


const bindings = {
    appCapabilities: '<',
    capabilities: '<'
};


const template = require('./app-capability-table.html');


function refresh(appCapabilities = [], capabilities= []) {
    const capabilitiesById = _.keyBy(capabilities, 'id');

    return _.map(appCapabilities, ac => {
        return Object.assign(
            {},
            ac,
            { capability: capabilitiesById[ac.capabilityId] });
    });
}


function controller() {
    const vm = this;

    vm.$onChanges = () => {
        vm.items = refresh(vm.appCapabilities, vm.capabilities);
    };
}


const component =  {
    template,
    controller,
    bindings
};


export default component;
