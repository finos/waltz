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
import {initialiseData} from "../common";
import {CORE_API, getApiReference} from "../common/services/core-api-utils";
import toasts from "../svelte-stores/toast-store";
import template from "./entity-named-node-types-view.html";
import {displayError} from "../common/error-utils";


const initialState = {
    noteTypes: [],
    creatingNoteType: false,
    newNoteType: { }
};


function splitEntityKinds(entityKinds) {
    return _.split(entityKinds, /,\s*/);
}


function controller($q,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);
    const componentId = "entity-named-note-types-view";

    function update(noteType, change) {
        return serviceBroker
            .execute(CORE_API.EntityNamedNoteTypeStore.update, [noteType.id, change])
            .then(() => {
                loadNoteTypes(true);
                toasts.success("Updated");
            })
            .catch(e => displayError(null, "Failed to save note definition", e));
    }

    vm.updateName = (change, type) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(type, { name: change.newVal });
    };

    vm.updateExternalId = (change, type) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(type, { externalId: change.newVal });
    };

    vm.updateDescription = (change, type) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(type, { description: change.newVal });
    };

    vm.updateIsReadOnly = (change, type) => {
        return update(type, { isReadOnly: change.newVal });
    };

    vm.updatePosition = (change, type) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Cannot be blank");
        return update(type, { position: change.newVal });
    };

    vm.updateApplicableEntityKinds = (change, type) => {
        if (_.isNil(change.newVal) || change.newVal === "") return $q.reject("Too short");
        return update(type, { applicableEntityKinds: splitEntityKinds(change.newVal) });
    };

    vm.startNewNoteType = () => {
        vm.creatingNoteType = true;
    };

    vm.saveNewNoteType = () => {
        const params = [{
            name: vm.newNoteType.name,
            description: vm.newNoteType.description,
            applicableEntityKinds: splitEntityKinds(vm.newNoteType.applicableEntityKinds),
            isReadOnly: vm.newNoteType.isReadOnly,
            position: vm.newNoteType.position,
            externalId: vm.newNoteType.externalId
        }];

        return serviceBroker
            .execute(CORE_API.EntityNamedNoteTypeStore.create, params)
            .then(() => {
                toasts.success("Created new note type: "+ vm.newNoteType.name);
                vm.creatingNoteType = false;
                vm.newNoteType = {};
                loadNoteTypes(true);
            });
    };

    vm.deleteNoteType = (id) => {
        if (confirm("Are you sure you want to delete this note type?")) {
            return serviceBroker
                .execute(CORE_API.EntityNamedNoteTypeStore.remove, [id])
                .then((r) => {
                    if (r.data) {
                        toasts.success("Deleted");
                        loadNoteTypes(true);
                    } else {
                        toasts.error("Failed to delete, ensure that note type is not being used");
                    }
                });
        }
    };

    vm.cancelNewNoteType = () => {
        vm.creatingNoteType = false;
    };


    function loadNoteTypes(force = false) {
        const options = {
            force,
            cacheRefreshListener: {
                componentId,
                fn: cacheRefreshListener
            }
        };

        serviceBroker
            .loadAppData(
                CORE_API.EntityNamedNoteTypeStore.findAll,
                [],
                options)
            .then(result => {
                vm.noteTypes = result.data;
            });
    }

    const cacheRefreshListener = (e) => {
        if (e.eventType === "REFRESH"
            && getApiReference(e.serviceName, e.serviceFnName) === CORE_API.EntityNamedNoteTypeStore.findAll) {
            loadNoteTypes();
        }
    };

    loadNoteTypes();
}


controller.$inject = [
    "$q",
    "ServiceBroker"
];


export default {
    template,
    controller,
    controllerAs: "ctrl",
    bindToController: true,
    scope: {}
};
