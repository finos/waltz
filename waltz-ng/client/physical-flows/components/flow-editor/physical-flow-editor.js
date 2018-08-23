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
import template from "./physical-flow-editor.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);


    const load = () => {
        serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.getById,
                [vm.parentEntityRef.id],
                {force: true})
            .then(r => vm.physicalFlow = r.data);
    };


    vm.$onInit = () => load();


    const mkCommand = (name, value) => {
        return {
            entityReference: toEntityRef(vm.physicalFlow, 'PHYSICAL_FLOW'),
            name,
            value
        };
    };

    const doSave = (name, value) => {
        const cmd = mkCommand(name, value);
        return serviceBroker
            .execute(
                CORE_API.PhysicalFlowStore.updateAttribute,
                [ vm.physicalFlow.id, cmd ])
            .then(r => {
                notification.success('Updated');
                return load();
            })
            .catch(e => notification.error('Could not update value'))
    };

    vm.onSaveCriticality = (value, ctx) => doSave('criticality', value);
    vm.onSaveFrequency = (value, ctx) => doSave('frequency', value);
    vm.onSaveTransport = (value, ctx) => doSave('transport', value);
    vm.onSaveBasisOffset = (itemId, value) => doSave('basisOffset', value.newVal);
    vm.onSaveDescription = (itemId, value) => doSave('description', value.newVal);
}


controller.$inject = [
    'Notification',
    'ServiceBroker'
];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: 'waltzPhysicalFlowEditor'
};