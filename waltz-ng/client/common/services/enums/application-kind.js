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

export const applicationKind = {
    CUSTOMISED: {
        key: "CUSTOMISED",
        name: "Customised",
        icon: null,
        description: "Thirdparty application which has been customised",
        position: 10
    },
    EUC: {
        key: "EUC",
        name: "End User Computing",
        icon: null,
        description: "End user produced applications",
        position: 20
    },
    EXTERNAL: {
        key: "EXTERNAL",
        name: "External to the organisation",
        icon: null,
        description: "Applications which are completely external to the organization",
        position: 30
    },
    EXTERNALLY_HOSTED: {
        key: "EXTERNALLY_HOSTED",
        name: "Externally Hosted",
        icon: null,
        description: "Applications which are hosted externally, typically SaaS solutions",
        position: 40
    },
    IN_HOUSE: {
        key: "IN_HOUSE",
        name: "In House",
        icon: null,
        description: "Applications which have been developed internally",
        position: 50
    },
    INTERNALLY_HOSTED: {
        key: "INTERNALLY_HOSTED",
        name: "Hosted Internally",
        icon: null,
        description: "Thirdparty applications which are hosted on premises",
        position: 60
    },
    THIRD_PARTY: {
        key: "THIRD_PARTY",
        name: "Third Party",
        icon: null,
        description: "Applications developed by a third party",
        position: 70
    },
    UNKNOWN: {
        key: "UNKNOWN",
        name: "Unknown",
        icon: null,
        description: "Applications of an unknown type",
        position: 99
    }
};
