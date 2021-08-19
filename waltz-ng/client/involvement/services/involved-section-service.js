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

function service(involvementStore) {

    const addInvolvement = (entityRef, entityInvolvement) => {

        return involvementStore.changeInvolvement(
            entityRef,
            mkChangeCommand("ADD", entityInvolvement.entity, entityInvolvement.involvement))
            .then(result => {
                if(result) {
                    toasts.success("Involvement added successfully");
                } else {
                    toasts.warning("Failed to add involvement")
                }
            });
    };


    const removeInvolvement = (entityRef, entityInvolvement) => {

        return involvementStore.changeInvolvement(
            entityRef,
            mkChangeCommand("REMOVE", entityInvolvement.entity, entityInvolvement.involvement))
            .then(result => {
                if(result) {
                    toasts.success("Involvement removed successfully");
                } else {
                    toasts.warning("Failed to remove involvement")
                }
            });
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
