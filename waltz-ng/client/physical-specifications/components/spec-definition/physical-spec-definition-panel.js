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


const bindings = {
    defaultActiveTabIndex: '<',
    logicalDataElements: '<',
    specDefinition: '<',
    selectableDefinitions: '<',
    onDefinitionSelect: '<'
};


const initialState = {
    logicalDataElementsById: {},
    specDefinition: {},
    selectableDefinitions: [],
    onDefinitionSelect: (def) => console.log('psdp::onDefinitionSelect', def)
};


const template = require('./physical-spec-definition-panel.html');


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if(vm.logicalDataElements) {
            vm.logicalDataElementsById = _.keyBy(vm.logicalDataElements, 'id');
            console.log('log els: ', vm.logicalDataElementsById);
        }
    };

    vm.definitionSelected = (def) => invokeFunction(vm.onDefinitionSelect, def);
}


controller.$inject = [];


const component = {
    controller,
    template,
    bindings,
    transclude: {
        noDataMessage: 'noDataMessage'
    }
};


export default component;