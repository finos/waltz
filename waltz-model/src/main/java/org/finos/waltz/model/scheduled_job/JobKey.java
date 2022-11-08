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

package org.finos.waltz.model.scheduled_job;

public enum JobKey {
    HIERARCHY_REBUILD_CHANGE_INITIATIVE,
    HIERARCHY_REBUILD_DATA_TYPE,
    HIERARCHY_REBUILD_ENTITY_STATISTICS,
    HIERARCHY_REBUILD_MEASURABLE,
    HIERARCHY_REBUILD_ORG_UNIT,
    HIERARCHY_REBUILD_PERSON,

    DATA_TYPE_RIPPLE_PHYSICAL_TO_LOGICAL,
    DATA_TYPE_USAGE_RECALC_APPLICATION,
    AUTH_SOURCE_RECALC_FLOW_RATINGS,
    LOGICAL_FLOW_CLEANUP_ORPHANS,
    ATTESTATION_CLEANUP_ORPHANS,
    ATTESTATION_ISSUE_INSTANCES,

    SURVEY_INSTANCE_REASSIGN_RECIPIENTS,

    REPORT_GRID_RECALCULATE_APP_GROUPS_FROM_FILTERS
}
