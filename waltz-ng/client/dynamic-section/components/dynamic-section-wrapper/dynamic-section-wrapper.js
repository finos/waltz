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

import {initialiseData} from "../../../common/index";

import template from './dynamic-section-wrapper.html';
import {sectionToTemplate} from "../../dynamic-section-utils";


const bindings = {
    parentEntityRef: '<',
    section: '<',
    onRemove: '<',
};


const initialState = {
};


function controller($element, $compile, $scope) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const sectionScope = $scope.$new();
        sectionScope.parentEntityRef = vm.parentEntityRef;
        sectionScope.section = vm.section;
        sectionScope.onRemove = vm.onRemove;
        sectionScope.canRemove = vm.canRemove;

        const linkFn = $compile(sectionToTemplate(vm.section));
        const content = linkFn(sectionScope);
        $element.append(content);
    };
}


controller.$inject=[
    '$element',
    '$compile',
    '$scope'
];


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: 'waltzDynamicSectionWrapper'
}