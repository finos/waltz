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

import template from "./related-data-types-section.html";
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from "lodash";
import {toEntityRef} from "../../../common/entity-utils";
import {mkRel} from "../../../common/relationship-utils";

const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    visibility: {
        picker: false
    }
};


function alreadyContains(relatedDataTypes = [], dt) {
    const existingIds = _.map(relatedDataTypes, "id");
    return _.includes(existingIds, dt.id);
}


function controller(serviceBroker, notification) {

    const vm = initialiseData(this, initialState);

    function refresh() {
        if (vm.parentEntityRef === null || vm.dataTypes === null) {
            return;
        }

        const dataTypesById = _.keyBy(vm.dataTypes, "id");
        serviceBroker
            .loadViewData(
                CORE_API.EntityRelationshipStore.findForEntity,
                [ vm.parentEntityRef ],
                { force: true })
            .then(r => {
                vm.relatedDataTypes = _
                    .chain(r.data)
                    .filter(rel => rel.b.kind === "DATA_TYPE")
                    .map(rel => Object.assign({}, dataTypesById[rel.b.id]))
                    .sortBy("name")
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
        vm.editMode = alreadyContains(vm.relatedDataTypes, vm.selectedDataType)
            ? "REMOVE"
            : "ADD";
    };

    vm.onAction = () => {
        const isRemove = alreadyContains(vm.relatedDataTypes, vm.selectedDataType);

        const operation = isRemove
            ? CORE_API.EntityRelationshipStore.remove
            : CORE_API.EntityRelationshipStore.create;

        const rel = mkRel(vm.parentEntityRef, "RELATES_TO", toEntityRef(vm.selectedDataType));

        serviceBroker
            .execute(operation, [ rel ])
            .then(() => {
                const verb = isRemove
                    ? "removed"
                    : "added";
                const msg = `Relationship to ${vm.selectedDataType.name} ${verb}`;
                notification.success(msg);
                vm.selectedDataType = null;
                refresh();
            });
    };


    vm.onShowPicker = () => vm.visibility.picker = ! vm.visibility.picker;
}


controller.$inject= [ "ServiceBroker", "Notification"];


const component = {
    controller,
    bindings,
    template
};


export default {
    component,
    id: "waltzRelatedDataTypeSection"
};