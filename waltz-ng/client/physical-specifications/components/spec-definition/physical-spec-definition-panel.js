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
import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common";
import template from "./physical-spec-definition-panel.html";


const bindings = {
    defaultActiveTabIndex: '<',
    logicalDataElements: '<',
    specDefinition: '<',
    selectableDefinitions: '<',
    onDefinitionSelect: '<',
    onUpdateFieldDescription: '<',
    onUpdateLogicalDataElement: '<'
};


const initialState = {
    logicalDataElementsById: {},
    specDefinition: {},
    selectableDefinitions: [],
    onDefinitionSelect: (def) => console.log('psdp::onDefinitionSelect', def),
    onUpdateFieldDescription: (change, field) => console.log('psdp::onUpdateFieldDescription', { field, change }),
    onUpdateLogicalDataElement: (change, field) => console.log('psdp::onUpdateLogicalDataElement', { field, change })
};


function controller($q) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.logicalDataElements) {
            vm.logicalDataElementsById = _.keyBy(vm.logicalDataElements, 'id');
        }
    };

    vm.definitionSelected = (def) => invokeFunction(vm.onDefinitionSelect, def);

    vm.updateDescription = (change, f) => {
        if (_.isEmpty(change.newVal)) return $q.reject("Too short");
        return invokeFunction(vm.onUpdateFieldDescription, change, f);
    };

    vm.updateLogicalElement = (change, f) => {
        return invokeFunction(vm.onUpdateLogicalDataElement, change, f);
    };
}


controller.$inject = [
    '$q'
];


const component = {
    controller,
    template,
    bindings,
    transclude: {
        noDataMessage: 'noDataMessage'
    }
};


export default component;
