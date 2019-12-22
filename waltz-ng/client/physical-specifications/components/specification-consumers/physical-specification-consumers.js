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

import {initialiseData} from '../../../common';
import template from './physical-specification-consumers.html';

const bindings = {
    physicalFlows: '<',
    onFlowSelect: '<'
};


const initialState = {
    physicalFlows: [],
    onFlowSelect: (flow) => console.log('wpsc: on-flow-select', flow)
};


function controller() {
    initialiseData(this, initialState);
}


const component = {
    bindings,
    controller,
    template
};


export default component;
