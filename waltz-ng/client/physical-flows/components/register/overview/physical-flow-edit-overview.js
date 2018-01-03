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

import {initialiseData} from "../../../../common";
import template from './physical-flow-edit-overview.html';


const bindings = {
    sourceEntity: '<',
    specification: '<',
    targetLogicalFlow: '<',
    flowAttributes: '<',
    onSpecificationFocus: '<',
    onFlowAttributesFocus: '<',
    onTargetFocus: '<',
    onClone: '<'
};


const initialState = {
    onSpecificationFocus: () => console.log("No onSpecificationFocus handler defined for physical-flow-edit-overview"),
    onFlowAttributesFocus: () => console.log("No onFlowAttributesFocus handler defined for physical-flow-edit-overview"),
    onTargetFocus: () => console.log("No onTargetFocus handler defined for physical-flow-edit-overview")
};


function controller() {
    const vm = initialiseData(this, initialState);
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzPhysicalFlowEditOverview'
};