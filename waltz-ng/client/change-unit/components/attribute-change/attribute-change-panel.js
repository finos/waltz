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

import template from "./attribute-change-panel.html";


const bindings = {
    changeUnit: "<",
};


const initialState = {
    attributeChanges: [],
    attributeChangesParsed: [],
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
    };


    vm.$onChanges = (changes) => {
        if(changes.changeUnit) {
            serviceBroker
                .loadViewData(CORE_API.AttributeChangeStore.findByChangeUnitId, [vm.changeUnit.id])
                .then(r => r.data)
                .then(attributeChanges => {
                    vm.attributeChanges = attributeChanges;
                    vm.attributeChangesParsed = _.map(vm.attributeChanges, c => Object.assign({}, c, {
                        oldValueParsed: c.type === 'json' ? JSON.parse(c.oldValue) : c.oldValue,
                        newValueParsed: c.type === 'json' ? JSON.parse(c.newValue) : c.newValue,
                    }));
                });
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
    id: "waltzAttributeChangePanel"
};
