/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";
import { mkSelectionOptions } from "../../../common/selector-utils";

import template from "./person-change-set-section.html";


const bindings = {
    parentEntityRef: "<",
};


const initialState = {
    indirect: [],
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const loadDirect = () => serviceBroker
        .loadViewData(CORE_API.PersonStore.getById, [vm.parentEntityRef.id])
        .then(r => {
            vm.person = r.data;
            return serviceBroker
                .loadViewData(CORE_API.ChangeSetStore.findByPerson, [vm.person.employeeId])
                .then(r => vm.direct = r.data)
        });

    const loadIndirect = () => serviceBroker
        .loadViewData(CORE_API.ChangeSetStore.findBySelector, [mkSelectionOptions(vm.parentEntityRef)])
        .then(r => vm.indirect = r.data);


    vm.$onChanges = (changes) => {
        if(changes.parentEntityRef) {
            loadDirect()
                .then(() => loadIndirect());
        }
    };
}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzPersonChangeSetSection"
};
