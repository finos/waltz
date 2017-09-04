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

import template from './dyna-widget.html';
import {initialiseData} from "../../common/index";


const initialState = {
    additionalScope: {}
};


function controller($element, $compile, $scope) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const widgetScope = $scope.$new();
        widgetScope.parentEntityRef = vm.parentEntityRef;
        widgetScope.createDiagramCommands = "x"; // () => console.log('wtf');
        // Object.assign(widgetScope, vm.additionalScope);

        const linkFn = $compile(vm.widget.template);
        const content = linkFn(widgetScope);
        $element.append(content);
    };
}


controller.$inject=[
    '$element',
    '$compile',
    '$scope'
];


const bindings = {
    additionalScope: '<?',
    widget: '<',
    parentEntityRef: '<'
};


const component = {
    controller,
    template,
    bindings
};


export default {
    component,
    id: 'waltzDynaWidget'
}