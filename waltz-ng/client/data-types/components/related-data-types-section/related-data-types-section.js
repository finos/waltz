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