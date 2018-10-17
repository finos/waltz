
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

/**
 * Given an entity kind, this will return the matching
 * ui-router state name if avaialble.  Otherwise it
 * will throw an error.
 * @param kind
 * @returns String state name
 */
export function kindToViewState(kind) {
    if (kind === "APPLICATION") {
        return "main.app.view";
    }
    if (kind === "ACTOR") {
        return "main.actor.view";
    }
    if (kind === "APP_GROUP") {
        return "main.app-group.view";
    }
    if (kind === "DATA_TYPE") {
        return "main.data-type.view";
    }
    if (kind === "FLOW_DIAGRAM") {
        return "main.flow-diagram.view";
    }
    if (kind === "LOGICAL_DATA_ELEMENT") {
        return "main.logical-data-element.view";
    }
    if (kind === "LOGICAL_DATA_FLOW") {
        return "main.logical-flow.view";
    }
    if (kind === "MEASURABLE") {
        return "main.measurable.view";
    }
    if (kind === "MEASURABLE_CATEGORY") {
        return "main.measurable-category.view";
    }
    if (kind === "ORG_UNIT") {
        return "main.org-unit.view";
    }
    if (kind === "CHANGE_INITIATIVE") {
        return "main.change-initiative.view";
    }
    if (kind === "ENTITY_STATISTIC") {
        return "main.entity-statistic.view";
    }
    if (kind === "PERSON") {
        return "main.person.id";
    }
    if (kind === "PROCESS") {
        return "main.process.view";
    }
    if (kind === "PHYSICAL_SPECIFICATION") {
        return "main.physical-specification.view";
    }
    if (kind === "PHYSICAL_FLOW") {
        return "main.physical-flow.view";
    }
    if (kind === "ROADMAP") {
        return "main.roadmap.view";
    }
    throw "Unable to convert kind: "+kind+ " to a ui-view state";
}
