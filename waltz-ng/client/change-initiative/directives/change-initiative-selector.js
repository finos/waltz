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

//todo: make this a component (KS)

import {CORE_API} from '../../common/services/core-api-utils';
import template from './change-initiative-selector.html';


const BINDINGS = {
    model: '='
};


const initData = {
    results: []
};


function controller(serviceBroker) {
    const vm = Object.assign(this, initData);

    vm.refresh = (query) => {
        if (!query) return;
        serviceBroker
            .execute(CORE_API.ChangeInitiativeStore.search, [query])
            .then(result => vm.results = result.data);
    };

}


controller.$inject = [
    'ServiceBroker'
];


const directive = {
    restrict: 'E',
    replace: true,
    template,
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    scope: {}
};


export default () => directive;