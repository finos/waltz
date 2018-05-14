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

package com.khartec.waltz.model.scheduled_job;

public enum JobKey {
    HIERARCHY_REBUILD_CHANGE_INITIATIVE,
    HIERARCHY_REBUILD_DATA_TYPE,
    HIERARCHY_REBUILD_ENTITY_STATISTICS,
    HIERARCHY_REBUILD_MEASURABLE,
    HIERARCHY_REBUILD_ORG_UNIT,
    HIERARCHY_REBUILD_PERSON,

    DATA_TYPE_RIPPLE_PHYSICAL_TO_LOGICAL,
    DATA_TYPE_USAGE_RECALC_APPLICATION,
    COMPLEXITY_REBUILD,
    AUTH_SOURCE_RECALC_FLOW_RATINGS,
    LOGICAL_FLOW_CLEANUP_ORPHANS,
    ATTESTATION_CLEANUP_ORPHANS
}
