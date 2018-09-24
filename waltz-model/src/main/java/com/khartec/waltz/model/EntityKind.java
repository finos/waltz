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

package com.khartec.waltz.model;

public enum EntityKind {

    APPLICATION,
    APP_GROUP,
    ASSET_COST,
    ATTESTATION,
    ATTESTATION_RUN,
    AUTHORITATIVE_SOURCE,
    BOOKMARK,
    CHANGE_INITIATIVE,
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
    ACTOR,
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
    SURVEY_INSTANCE,
    SURVEY_RUN,
    SURVEY_TEMPLATE,
    SYSTEM,

    @Deprecated
    CAPABILITY  // TO BE REMOVED IN 1.5
}
