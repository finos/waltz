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

import _ from "lodash";
import { toMap } from "./map-utils";


/***
 * A list of tuples of {<entityKind>, <uiState>}.  Used for to help with mappings
 * @type [ {<entityKind>, <uiState>}, ... ]
 */
const stateKindTuples = [
    {kind: "ACTOR", state: "main.actor.view"},
    {kind: "APPLICATION", state: "main.app.view"},
    {kind: "APP_GROUP", state: "main.app-group.view"},
    {kind: "CHANGE_INITIATIVE", state: "main.change-initiative.view"},
    {kind: "CHANGE_SET", state: "main.change-set.view"},
    {kind: "DATA_TYPE", state: "main.data-type.view"},
    {kind: "ENTITY_STATISTIC", state: "main.entity-statistic.view"},
    {kind: "FLOW_DIAGRAM", state: "main.flow-diagram.view"},
    {kind: "LICENCE", state: "main.licence.view"},
    {kind: "LOGICAL_DATA_ELEMENT", state: "main.logical-data-element.view"},
    {kind: "LOGICAL_DATA_FLOW", state: "main.logical-flow.view"},
    {kind: "MEASURABLE", state: "main.measurable.view"},
    {kind: "MEASURABLE_CATEGORY", state: "main.measurable-category.view"},
    {kind: "ORG_UNIT", state: "main.org-unit.view"},
    {kind: "PERSON", state: "main.person.id"},
    {kind: "PROCESS", state: "main.process.view"},
    {kind: "PHYSICAL_SPECIFICATION", state: "main.physical-specification.view"},
    {kind: "PHYSICAL_FLOW", state: "main.physical-flow.view"},
    {kind: "ROADMAP", state: "main.roadmap.view"},
    {kind: "SCENARIO", state: "main.scenario.view"},
    {kind: "SERVER", state: "main.server.view"},
    {kind: "SOFTWARE", state: "main.software-package.view"},
    {kind: "TAG", state: "main.tag.id.view"}
];


const kindsToViewStateMap = toMap(
    stateKindTuples,
    t => t.kind,
    t => t.state);


const viewStateToKindMap = _.assign(
    toMap(stateKindTuples,
          t => t.state,
          t => t.kind),
    { "main.person.view": "PERSON" });


/**
 * Given an entity kind, this will return the matching
 * ui-router state name if available.  Otherwise it
 * will throw an error.
 * @param kind
 * @returns String state name
 */
export function kindToViewState(kind) {
    return _.result(
        kindsToViewStateMap,
        kind,
        () => { throw `Unable to convert kind: ${kind} to a ui-view state`; });
}


/**
 * Given an view state, this will return the matching
 * entity kind if available.  Otherwise it
 * will throw an error.
 * @param viewState  state name
 * @returns kind
 */
export function viewStateToKind(viewState) {
    return _.result(
        viewStateToKindMap,
        viewState,
        () => { throw "Unable to convert view state: "+viewState+ " to an entity kind"; });
}
