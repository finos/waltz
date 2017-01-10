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
import {initialiseData} from "../../../common";

const bindings = {
    application: '<',
    usages: '<',
    capabilities: '<',
    save: '<',
    remove: '<',
    update: '<'
};


const template = require('./app-capability-editor.html');


const initialState = {
    mode: 'NONE', // NONE, EDIT, ADD
    capabilities: [],  // all
    usages: []
};


function refresh(capabilities = [], usages = []) {

    const capabilitiesById = _.keyBy(capabilities, 'id');

    return _.map(usages, u => Object.assign(
        {} ,
        u,
        { capability: capabilitiesById[u.capabilityId]}));
}


function controller() {

    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        vm.currentUsage = refresh(vm.capabilities, vm.usages);
    };


    vm.showAddNew = () => {
        vm.selected = {
            capabilityId: null,
            comment: "",
            rating: vm.application.overallRating
        };
        vm.changeMode('ADD');
    };


    vm.changeMode = (mode) => {
        vm.mode = mode;
    };


    vm.showEdit = (usage) => {
        vm.selected = usage;
        vm.changeMode('EDIT');
    };

    vm.cancelEdit = () => {
        vm.changeMode('NONE');
        vm.selected = null;
    };

    vm.onSave = (obj) => {
        return vm.save(obj)
            .then(() => vm.changeMode('NONE'))
            .then(() => vm.selected = null);
    };


    vm.onRemove = (usage) => {
        if (confirm(`Remove ${usage.capability.name} ?`)) {
            vm.selected = null;
            vm.changeMode('NONE');
            return vm.remove(usage.capability);
        }
    };

}



controller.$inject = ['$scope'];


const component = {
    bindings,
    template,
    controller
};


export default component;