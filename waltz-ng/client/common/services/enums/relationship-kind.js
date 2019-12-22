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

export const relationshipKind = {
    HAS: {
        key: "HAS",
        name: "Has",
        icon: null,
        description: null,
        position: 10
    },
    DEPRECATES: {
        key: "DEPRECATES",
        name: "Deprecates",
        icon: null,
        description: null,
        position: 20
    },
    LOOSELY_RELATES_TO: {
        key: "LOOSELY_RELATES_TO",
        name: "Loosely Relates To",
        icon: null,
        description: null,
        position: 30
    },
    PARTICIPATES_IN: {
        key: "PARTICIPATES_IN",
        name: "Participates In",
        icon: null,
        description: null,
        position: 40
    },
    RELATES_TO: {
        key: "RELATES_TO",
        name: "Relates To",
        icon: null,
        description: null,
        position: 50
    },
    SUPPORTS: {
        key: "SUPPORTS",
        name: "Supports",
        icon: null,
        description: null,
        position: 60
    },
    APPLICATION_NEW: {
        key: "APPLICATION_NEW",
        name: "Application - new",
        icon: null,
        description: null,
        position: 70
    },
    APPLICATION_FUNCTIONAL_CHANGE: {
        key: "APPLICATION_FUNCTIONAL_CHANGE",
        name: "Application - functional change",
        icon: null,
        description: null,
        position: 80
    },
    APPLICATION_DECOMMISSIONED: {
        key: "APPLICATION_DECOMMISSIONED",
        name: "Application - decommissioned",
        icon: null,
        description: null,
        position: 90
    },
    APPLICATION_NFR_CHANGE: {
        key: "APPLICATION_NFR_CHANGE",
        name: "Application - NFR change",
        icon: null,
        description: null,
        position: 100
    },
    DATA_PUBLISHER: {
        key: "DATA_PUBLISHER",
        name: "Data publisher",
        icon: null,
        description: null,
        position: 110
    },
    DATA_CONSUMER: {
        key: "DATA_CONSUMER",
        name: "Data consumer",
        icon: null,
        description: null,
        position: 120
    }
};