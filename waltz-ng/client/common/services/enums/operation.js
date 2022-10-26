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

export const operation = {
    ADD: {
        key: "ADD",
        name: "Add",
        icon: null,
        description: null,
        position: 10
    },
    ATTEST: {
        key: "ATTEST",
        name: "Attest",
        icon: null,
        description: null,
        position: 20
    },
    REMOVE: {
        key: "REMOVE",
        name: "Remove",
        icon: null,
        description: null,
        position: 30
    },
    UPDATE: {
        key: "UPDATE",
        name: "Update",
        icon: null,
        description: null,
        position: 30
    },
    UNKNOWN: {
        key: "UNKNOWN",
        name: "Unknown",
        icon: null,
        description: null,
        position: 30
    }
};


export const editOperations = [operation.ADD.key, operation.REMOVE.key, operation.UPDATE.key];