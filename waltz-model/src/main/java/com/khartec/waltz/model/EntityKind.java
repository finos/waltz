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

public enum EntityKind {

    ACTOR,
    ALLOCATION_SCHEME,
    APPLICATION,
    APP_GROUP,
    ASSET_COST,
    ATTESTATION,
    ATTESTATION_RUN,
    AUTHORITATIVE_SOURCE,
    BOOKMARK,
    CHANGE_INITIATIVE,
    CHANGE_SET,
    CHANGE_UNIT,
    DATABASE,
    DATA_TYPE,
    DRILL_GRID_DEFINITION,
    END_USER_APPLICATION,
    ENTITY_HIERARCHY,
    ENTITY_NAMED_NOTE,
    ENTITY_NAMED_NOTE_TYPE,
    ENTITY_STATISTIC,
    FLOW_DIAGRAM,
    FLOW_ANNOTATION,
    INVOLVEMENT,
    INVOLVEMENT_KIND,
    LICENCE,
    LOGICAL_DATA_ELEMENT,
    LOGICAL_DATA_FLOW,
    MEASURABLE,
    MEASURABLE_CATEGORY,
    MEASURABLE_RATING,
    ORG_UNIT,
    PERFORMANCE_METRIC_PACK,
    PERSON,
    PHYSICAL_SPECIFICATION,
    PHYSICAL_SPEC_DEFN,
    PHYSICAL_SPEC_DEFN_FIELD,
    PHYSICAL_FLOW,
    ROADMAP,
    SCENARIO,
    SERVER,
    SOFTWARE,
    SOFTWARE_VERSION,
    SURVEY_INSTANCE,
    SURVEY_RUN,
    SURVEY_TEMPLATE,
    SYSTEM,
    TAG,

    @Deprecated
    CAPABILITY  // TO BE REMOVED IN 1.5
    ;
}
