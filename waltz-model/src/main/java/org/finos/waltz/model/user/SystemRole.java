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

package org.finos.waltz.model.user;

import org.finos.waltz.common.EnumUtilities;

import java.util.Set;


public enum SystemRole {
    ACTOR_ADMIN,
    ADMIN,
    AGGREGATE_OVERLAY_DIAGRAM_EDITOR,
    APP_EDITOR,
    ANONYMOUS,
    ASSESSMENT_DEFINITION_ADMIN,
    ATTESTATION_ADMIN,
    AUTHORITATIVE_SOURCE_EDITOR,
    BETA_TESTER,
    BULK_FLOW_EDITOR,
    BULK_LEGAL_ENTITY_RELATIONSHIP_EDITOR,
    BOOKMARK_EDITOR,
    CAPABILITY_EDITOR,
    CHANGE_INITIATIVE_EDITOR,
    CHANGE_SET_EDITOR,
    EUDA_ADMIN,
    INVOLVEMENT_EDITOR,
    LINEAGE_EDITOR,
    LOGICAL_DATA_FLOW_EDITOR,
    ORG_UNIT_EDITOR,
    PHYSICAL_SPECIFICATION_EDITOR,
    RATING_EDITOR,
    RATING_SCHEME_ADMIN,
    REPORT_GRID_ADMIN,
    SCENARIO_ADMIN,
    SCENARIO_EDITOR,
    SURVEY_ADMIN,
    SURVEY_TEMPLATE_ADMIN,
    TAXONOMY_EDITOR,
    USER_ADMIN;


    public static Set<String> allNames() {
        return EnumUtilities.names(SystemRole.values());
    }
}
