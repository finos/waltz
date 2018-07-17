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
import template from './app-authority-panel.html';
import {initialiseData} from "../../../common/index";
import {nest} from "d3-collection";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    authSources: '<'
};


const initialState = {
    nestedAuthSources: null
};


function controller(serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnitsById = _
                .chain(r.data)
                .map(ou => Object.assign({}, ou, { kind: 'ORG_UNIT' }))
                .keyBy('id')
                .value());
    };

    vm.$onChanges = () => {
        vm.nestedAuthSources = nest()
            .key(a => a.dataType)
            .key(a => a.rating)
            .object(vm.authSources || []);
    };

}


controller.$inject = ['ServiceBroker'];


const component = {
    controller,
    bindings,
    template
};


const id = 'waltzAppAuthorityPanel';

export default {
    id,
    component
};






