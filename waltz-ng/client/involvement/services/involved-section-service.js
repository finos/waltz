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

import {mkChangeCommand} from "../involvement-utils";
import toasts from "../../svelte-stores/toast-store";
import {displayError} from "../../common/error-utils";

function service(involvementStore) {

    const addInvolvement = (entityRef, entityInvolvement) => {

        return involvementStore.changeInvolvement(
            entityRef,
            mkChangeCommand("ADD", entityInvolvement.entity, entityInvolvement.involvement))
            .then(successful => {
                if (successful) {
                    toasts.success("Involvement added successfully");
                } else {
                    toasts.warning("Involvement was not added, it may already exist");
                }
            })
            .catch(e => displayError("Failed to add involvement", e));
    };


    const removeInvolvement = (entityRef, entityInvolvement) => {

        return involvementStore
            .changeInvolvement(
                entityRef,
                mkChangeCommand("REMOVE", entityInvolvement.entity, entityInvolvement.involvement))
            .then(successful => {
                if (successful) {
                    toasts.success("Involvement removed successfully");
                } else {
                    toasts.warning("Involvement was not removed, it may have already been removed");
                }
            })
            .catch(e => displayError("Failed to remove involvement", e));
    };


    return {
        addInvolvement,
        removeInvolvement
    };
}


service.$inject = [
    "InvolvementStore",
];


export default service;
