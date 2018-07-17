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

import angular from "angular";
import {CORE_API} from "../../../common/services/core-api-utils";
import template from './person-home.html';


const initialState = {
    person: null
};


function controller($state,
                    serviceBroker) {

    const vm = Object.assign(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(
                CORE_API.SvgDiagramStore.findByGroup,
                [ 'ORG_TREE' ])
            .then(r => vm.diagrams = r.data);
    };

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.person.view', { empId: b.value });
        angular.element(b.block).addClass('clickable');
    };

    vm.goToPerson = (person) => {
        $state.go('main.person.id', { id: person.id });
    };

}


controller.$inject = [
    '$state',
    'ServiceBroker'
];


const view = {
    template,
    controllerAs: 'ctrl',
    controller
};


export default view;
