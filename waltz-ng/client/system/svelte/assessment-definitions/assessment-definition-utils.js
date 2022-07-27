
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019, 2020, 2021 Waltz open source project
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

import {writable} from "svelte/store";

export function getRequiredFields(d) {
    return [d.name, d.entityKind, d.description];
}

export const possibleEntityKinds = [
    {value: "APPLICATION", name: "Application"},
    {value: "CHANGE_INITIATIVE", name: "Change Initiative"},
    {value: "CHANGE_SET", name: "Change Set"},
    {value: "ENTITY_RELATIONSHIP", name: "Entity Relationship"},
    {value: "LICENCE", name: "Software Licence"},
    {value: "LOGICAL_DATA_FLOW", name: "Logical Data Flow"},
    {value: "MEASURABLE", name: "Measurable"},
    {value: "PHYSICAL_FLOW", name: "Physical Flow"},
    {value: "PHYSICAL_SPECIFICATION", name: "Physical Specification"},
    {value: "SOFTWARE_PACKAGE", name: "Software Package"}
];

export const possibleVisibility = [
    {value: "PRIMARY", name: "Primary"},
    {value: "SECONDARY", name: "Secondary"}
];

export const selectedDefinition = writable(null);