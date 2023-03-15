
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
    {value: "APPLICATION", name: "Application", qualifierKind: null},
    {value: "CHANGE_INITIATIVE", name: "Change Initiative", qualifierKind: null},
    {value: "CHANGE_SET", name: "Change Set", qualifierKind: null},
    {value: "ENTITY_RELATIONSHIP", name: "Entity Relationship", qualifierKind: null},
    {value: "LEGAL_ENTITY", name: "Legal Entity", qualifierKind: null},
    {value: "LEGAL_ENTITY_RELATIONSHIP", name: "Legal Entity Relationship", qualifierKind: "LEGAL_ENTITY_RELATIONSHIP_KIND"},
    {value: "LICENCE", name: "Software Licence", qualifierKind: null},
    {value: "LOGICAL_DATA_FLOW", name: "Logical Data Flow", needsQualifier: false},
    {value: "MEASURABLE", name: "Measurable", qualifierKind: "MEASURABLE_CATEGORY"},
    {value: "PHYSICAL_FLOW", name: "Physical Flow", qualifierKind: null},
    {value: "PHYSICAL_SPECIFICATION", name: "Physical Specification", qualifierKind: null},
    {value: "SOFTWARE_PACKAGE", name: "Software Package", qualifierKind: null}
];

export const possibleVisibility = [
    {value: "PRIMARY", name: "Primary"},
    {value: "SECONDARY", name: "Secondary"}
];

export const selectedDefinition = writable(null);