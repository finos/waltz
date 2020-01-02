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


import template from './related-measurable-item-view.html';
import {initialiseData} from "../../../common/index";


const bindings = {
    item: '<',
    onDismiss: '<',
    onEdit: '<',
    onRemove: '<'
};


const initialState = {
    item: null,
    onDismiss: () => console.log('default on dismiss'),
    onEdit: () => console.log('default on edit'),
    onRemove: () => console.log('default on remove')
};



function controller() {
    initialiseData(this, initialState);
}


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: 'waltzRelatedMeasurableItemView'
}