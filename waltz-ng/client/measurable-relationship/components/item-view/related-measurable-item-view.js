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
import * as _ from "lodash";


const bindings = {
    items: '<',
    relationshipKinds: '<',
    onSelect: '<',
    onDismiss: '<',
    onEdit: '<',
    onRemove: '<',
    onAdd: '<'
};


const initialState = {
    selectedItem: null,
    displayInfo: false,
    relationshipKinds: [],
    onDismiss: () => console.log('default on dismiss'),
    onEdit: () => console.log('default on edit'),
    onRemove: () => console.log('default on remove'),
    onSelect: () => console.log('default on select'),
    onAdd: () => console.log('default on add')
};



function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if(c.items){
            vm.selectedItem = null;
            vm.list = vm.items;
            vm.counterpart = _.first(_.map(vm.items, i => (i.outbound) ? i.b : i.a));
        }
    };

    vm.selectItem = (item) => {
        vm.selectedItem = item;
        vm.onSelect(item);
    };

    vm.onAddItem = () => {
        vm.selectedItem = null;
        vm.onAdd();
    };

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