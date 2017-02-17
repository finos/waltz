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

import _ from "lodash";
import {notEmpty} from "../../../common";


const bindings = {
    flow: '<',
    decorators: '<',
    allDataTypes: '<',
    onSave: '<',
    onDelete: '<',
    onCancel: '<',
    onDirty: '<'
};


const template = require('./logical-flow-type-editor.html');


const initialState = {
    title: '-',
    flow: null,
    decorators: [],
    allDataTypes: [],
    checkedItemIds: [],
    expandedItemIds: [],
    originalSelectedItemIds: [],
    saving: false,
    onSave: (x) => console.log('lfte: default onSave()', x),
    onDelete: (x) => console.log('lfte: default onDelete()', x),
    onCancel: (x) => console.log('lfte: default onCancel()', x),
    onDirty: (x) => console.log('lfte: default onDirty()', x)
};


function isDirty(selectedIds = [], originalSelectedIds = []) {
    return !_.isEqual(selectedIds.sort(), originalSelectedIds.sort());
}


function anySelected(selectedIds = []) {
    return notEmpty(selectedIds);
}


function mkTitle(flow) {
    return flow
        ? `Datatypes sent from ${flow.source.name} to ${flow.target.name}`
        : '?';
}


function mkSelectedTypeIds(decorators = []) {
    return _.chain(decorators)
        .filter(d => d.decoratorEntity.kind === 'DATA_TYPE')
        .map('decoratorEntity.id')
        .value();
}


function mkUpdateCommand(flow, selectedIds = [], originalIds = []) {
    const addedIds = _.difference(selectedIds, originalIds);
    const removedIds = _.difference(originalIds, selectedIds);

    const command = {
        flowId: flow.id,
        addedDecorators: _.map(addedIds, id => ({ id, kind: 'DATA_TYPE' })),
        removedDecorators: _.map(removedIds, id => ({ id, kind: 'DATA_TYPE' }))
    };

    return command;
}


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        vm.title = mkTitle(vm.flow);
        vm.checkedItemIds = mkSelectedTypeIds(vm.decorators);
        vm.originalSelectedItemIds = mkSelectedTypeIds(vm.decorators);
        vm.expandedItemIds = mkSelectedTypeIds(vm.decorators);
    };

    vm.save = () => {
        if(vm.saving) return;
        vm.saving = true;
        const command = mkUpdateCommand(vm.flow, vm.checkedItemIds, vm.originalSelectedItemIds);
        vm.onSave(command)
            .then(() => vm.saving = false);
    };

    vm.delete = () => vm.onDelete(vm.flow);
    vm.cancel = () => vm.onCancel();
    vm.onChange = () => vm.onDirty(isDirty(vm.checkedItemIds, vm.originalSelectedItemIds));
    vm.canSave = () => isDirty(vm.checkedItemIds, vm.originalSelectedItemIds)
                        && anySelected(vm.checkedItemIds)
                        && !vm.saving;
    vm.anySelected = () => anySelected(vm.checkedItemIds);

    vm.typeSelected = (id) => {};

    vm.typeChecked = (id) => {
        vm.checkedItemIds = _.union(vm.checkedItemIds, [id])
    };

    vm.toggleTypeChecked = (id) => {
        _.some(vm.checkedItemIds, x => x === id)
            ? vm.typeUnchecked(id)
            : vm.typeChecked(id);
    };

    vm.typeUnchecked = (id) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
    };
}


const component = {
    bindings,
    controller,
    template
};


export default component;