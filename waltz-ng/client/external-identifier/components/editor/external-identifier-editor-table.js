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

import {initialiseData} from "../../../common/index";
import template from "./external-identifier-editor-table.html";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRef} from "../../../common/entity-utils";
import {displayError} from "../../../common/error-utils";


const bindings = {
    physicalFlow: "<",
    editable: "<"
};


const initialState = {
    editable: false,
    newExternalId: ""
};


function controller(notification, serviceBroker) {
    const vm = initialiseData(this, initialState);

    const load = () => {
        serviceBroker
            .loadViewData(
                CORE_API.ExternalIdentifierStore.findByEntityReference,
                [vm.entityRef],
                {force: vm.editable})
            .then(r => vm.externalIdentifiers = r.data);
    };

    vm.$onChanges = () => {
        if (vm.physicalFlow) {
            vm.entityRef = mkRef(vm.physicalFlow.kind, vm.physicalFlow.id);
            load();
        }
    };

    vm.removeExternalId = (externalIdentifier) => {
        if (confirm(`Are you sure you want to delete externalId ${externalIdentifier.externalId}?`)) {
            return serviceBroker
                .execute(
                    CORE_API.ExternalIdentifierStore.deleteExternalIdentifier,
                    [vm.entityRef,
                        externalIdentifier.externalId,
                        externalIdentifier.system
                    ])
                .then(() => {
                    notification.success(`Deleted External Id ${externalIdentifier.externalId}`);
                    return load();
                })
                .catch(e => displayError(notification, "Could not delete value", e))
        }

    };

    vm.addNewExternalId = () => {
        if(!_.isEmpty(vm.newExternalId)) {
            return serviceBroker
                .execute(
                    CORE_API.ExternalIdentifierStore.addExternalIdentifier,
                    [vm.entityRef, vm.newExternalId])
                .then(() => {
                    notification.success(`Added External Id ${vm.newExternalId}`);
                    vm.newExternalId = null;
                    load();
                })
                .catch(e => displayError(notification, "Could not add value", e))
        }
    }
}

controller.$inject = [
    "Notification",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzExternalIdentifierEditorTable"
};
