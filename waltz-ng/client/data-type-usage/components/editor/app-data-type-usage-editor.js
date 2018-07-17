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

