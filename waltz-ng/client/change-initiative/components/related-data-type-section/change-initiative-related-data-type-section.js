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

import template from './change-initiative-related-data-type-section.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";

const bindings = {
    parentEntityRef: '<'
};

const initialState = {
    visibility: {
        picker: false
    }
};

function controller(serviceBroker, notification) {

    const vm = initialiseData(this, initialState);

    function refresh() {
        if (vm.parentEntityRef == null || vm.dataTypes == null) {
            return;
        }

        const dataTypesById = _.keyBy(vm.dataTypes, 'id');
        serviceBroker
            .loadViewData(
                CORE_API.ChangeInitiativeStore.findRelatedForId,
                [ vm.parentEntityRef.id ],
                { force: true })
            .then(r => {
                vm.relatedDataTypes = _
                    .chain(r.data)
                    .filter(rel => rel.b.kind === 'DATA_TYPE')
                    .map(rel => Object.assign({}, dataTypesById[rel.b.id], { kind: 'DATA_TYPE'}))
                    .sortBy('name')
                    .value()
            });
    }

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data)
            .then(r => refresh());
    };

    vm.$onRefresh = () => refresh();

    vm.onSelectDataType = (dt) => {
        vm.selectedDataType = dt;
        const existingIds = _.map(vm.relatedDataTypes, 'id');
        vm.editMode = _.includes(existingIds, dt.id)
            ? 'REMOVE'
            : 'ADD';
    };

    vm.onAction = () => {
        const operation = vm.editMode;

        const command = {
            operation,
            entityReference: {
                id: vm.selectedDataType.id,
                kind: 'DATA_TYPE',
                name: vm.selectedDataType.name
            },
            relationship: 'RELATES_TO'
        };

        serviceBroker
            .execute(
                CORE_API.ChangeInitiativeStore.changeRelationship,
                [vm.parentEntityRef.id, command])
            .then(() => {
                notification.success(`Relationship to ${vm.selectedDataType.name} ${vm.editMode === 'REMOVE' ? 'removed' : 'added'}`);
                vm.selectedDataType = null;
                refresh();
            });
    };


    vm.onShowPicker = () => vm.visibility.picker = ! vm.visibility.picker;
}


controller.$inject= [ 'ServiceBroker', 'Notification'];


export const component = {
    controller,
    bindings,
    template
};


export const id = 'waltzChangeInitiativeRelatedDataTypeSection';