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

import _ from "lodash";
import allUsageKinds from "../../usage-kinds";
import {initialiseData} from "../../../common";
import template from './app-data-type-usage-editor.html';


const bindings = {
    primaryEntity: '<',
    type: '<',
    usages: "<",
    onCancel: "<",
    onSave: "<"
};


const initialState = {
    rows: [],
    onCancel: () => 'no onCancel handler provided for app-data-type-usage-editor',
    onSave: () => 'no onSave handler provided for app-data-type-usage-editor'
};


function prepareSave(usageRows = []) {
    const usages = _.map(usageRows, row => {
        return {
            kind: row.kind,
            isSelected: row.selected,
            description: row.description
        }
    });
    return usages;
}


function mkUsageRows(usages = []) {
    const usagesByKind = _.keyBy(usages, 'kind');

    return _.chain(allUsageKinds)
        .map(usageKind => {
            const currentUsageForThisKind = usagesByKind[usageKind.kind];
            return Object.assign({}, usageKind, {
                description: currentUsageForThisKind
                    ? currentUsageForThisKind.description
                    : '',
                selected: currentUsageForThisKind != null
                && currentUsageForThisKind.isSelected
            });
        })
        .orderBy('kind')
        .value()
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => vm.usageRows = mkUsageRows(vm.usages);
    vm.save = () => vm.onSave(prepareSave(vm.usageRows));
    vm.cancel = () => vm.onCancel();
}


controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default component;

