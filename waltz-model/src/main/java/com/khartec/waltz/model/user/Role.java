/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.model.user;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public enum Role {
    ADMIN,
    APP_EDITOR,
    ANONYMOUS,
    ATTESTATION_ADMIN,
    AUTHORITATIVE_SOURCE_EDITOR,
    BETA_TESTER,
    BOOKMARK_EDITOR,
    CAPABILITY_EDITOR,
    CHANGE_INITIATIVE_EDITOR,
    LINEAGE_EDITOR,
    LOGICAL_DATA_FLOW_EDITOR,
    ORG_UNIT_EDITOR,
    RATING_EDITOR,
    SCENARIO_ADMIN,
    SCENARIO_EDITOR,
    SURVEY_ADMIN,
    SURVEY_TEMPLATE_ADMIN,
    TAXONOMY_EDITOR,
    USER_ADMIN
    ;


    public static Set<String> allNames() {
        return Stream
                .of(Role.values())
                .map(r -> r.name())
                .collect(Collectors.toSet());
    }
}
