/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
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
    initialiseData(this, initialState);
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