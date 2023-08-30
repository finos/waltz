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


import systemRoles from "../user/system-roles";

/**
 * Given an entity kind, returns the user roles required for editing
 *
 * Note this file should correspond to the Java impl:
 * `org.finos.waltz.service.user.RoleUtilities.getRequiredRoleForEntityKind(...)`
 *
 * @param kind
 * @param secondaryKind  (Optional) secondary kind
 * @returns string  key of role
 */
export function getEditRoleForEntityKind(kind, secondaryKind) {
    console.log({kind, secondaryKind});
    switch (kind) {
        case "APPLICATION":
            return systemRoles.APP_EDITOR.key;
        case "CHANGE_INITIATIVE":
            return systemRoles.CHANGE_INITIATIVE_EDITOR.key;
        case "MEASURABLE":
            return secondaryKind
                ? systemRoles.CAPABILITY_EDITOR.key
                : systemRoles.TAXONOMY_EDITOR.key;
        case "ORG_UNIT":
            return systemRoles.ORG_UNIT_EDITOR.key;
        case "MEASURABLE_CATEGORY":
            return secondaryKind
                ? systemRoles.CAPABILITY_EDITOR.key
                : systemRoles.TAXONOMY_EDITOR.key;
        case "DATA_TYPE":
            return systemRoles.AUTHORITATIVE_SOURCE_EDITOR.key;
        case "SCENARIO":
            return systemRoles.SCENARIO_EDITOR.key;
        case "ROADMAP":
            return systemRoles.SCENARIO_ADMIN.key;
        default:
            return systemRoles.ADMIN.key;
    }
}