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

const usageKinds = [
    {
        displayName: 'Consumer',
        kind: "CONSUMER",
        nameDescription: "A consumer is an application that acquires data from an upstream source",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>incoming</strong> flows'
    }, {
        displayName: 'Distributor',
        kind: "DISTRIBUTOR",
        nameDescription: "A distributor is an application that provides data to one or more downstream applications",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>outgoing</strong> flows'
    }, {
        displayName: 'Modifier',
        kind: "MODIFIER",
        nameDescription: "A modifier is an application that changes the data acquired from an upstream source. Changes may be as a result of an application function or direct manipulation in a GUI",
        readOnly: false,
        readOnlyDescription: ''
    }, {
        displayName: 'Originator / Manual Entry',
        kind: "ORIGINATOR",
        nameDescription: "An originator is an application that captures data via a GUI or other input type (e.g. scanner, smartphone, card reader)",
        readOnly: true,
        readOnlyDescription: 'This value is derived from the presence of <strong>outgoing</strong> flows without any corresponding incoming flows'
    }
];


export default usageKinds;