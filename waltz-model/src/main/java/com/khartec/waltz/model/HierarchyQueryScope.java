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

package com.khartec.waltz.model;

public enum HierarchyQueryScope {
    EXACT,
    PARENTS,
    CHILDREN;


    public static HierarchyQueryScope determineUpwardsScopeForKind(EntityKind kind) {
        switch (kind) {
            case ORG_UNIT:
            case MEASURABLE:
            case CHANGE_INITIATIVE:
            case DATA_TYPE:
                return PARENTS;
            default:
                return EXACT;
        }
    }

    public static HierarchyQueryScope determineDownwardsScopeForKind(EntityKind kind) {
        switch (kind) {
            case ORG_UNIT:
            case MEASURABLE:
            case CHANGE_INITIATIVE:
            case DATA_TYPE:
                return CHILDREN;
            default:
                return EXACT;
        }
    }
}
