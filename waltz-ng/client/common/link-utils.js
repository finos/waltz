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

import _ from "lodash";
import {toMap} from "./map-utils";


/***
 * A list of tuples of {<entityKind>, <uiState>}.  Used for to help with mappings
 * @type [ {<entityKind>, <uiState>}, ... ]
 */
const stateKindTuples = [
    {kind: "ACTOR", state: "main.actor.view"},
    {kind: "AGGREGATE_OVERLAY_DIAGRAM_INSTANCE", state: "main.aggregate-overlay-diagram.instance-view"},
    {kind: "APPLICATION", state: "main.app.view"},
    {kind: "APP_GROUP", state: "main.app-group.view"},
    {kind: "CHANGE_INITIATIVE", state: "main.change-initiative.view"},
    {kind: "CHANGE_SET", state: "main.change-set.view"},
    {kind: "DATA_TYPE", state: "main.data-type.view"},
    {kind: "ENTITY_RELATIONSHIP", state: "main.entity-relationship.view"},
    {kind: "ENTITY_STATISTIC", state: "main.entity-statistic.view"},
    {kind: "FLOW_DIAGRAM", state: "main.flow-diagram.view"},
    {kind: "FLOW_CLASSIFICATION_RULE", state: "main.flow-classification-rule.view"},
    {kind: "INVOLVEMENT_KIND", state: "main.involvement-kind.view"},
    {kind: "LICENCE", state: "main.licence.view"},
    {kind: "LOGICAL_DATA_ELEMENT", state: "main.logical-data-element.view"},
    {kind: "LOGICAL_DATA_FLOW", state: "main.logical-flow.view"},
    {kind: "MEASURABLE", state: "main.measurable.view"},
    {kind: "MEASURABLE_CATEGORY", state: "main.measurable-category.view"},
    {kind: "ORG_UNIT", state: "main.org-unit.view"},
    {kind: "PERSON", state: "main.person.id"},
    {kind: "PROCESS", state: "main.process.view"},
    {kind: "PROCESS_DIAGRAM", state: "main.process-diagram.view"},
    {kind: "PHYSICAL_SPECIFICATION", state: "main.physical-specification.view"},
    {kind: "PHYSICAL_FLOW", state: "main.physical-flow.view"},
    {kind: "ROADMAP", state: "main.roadmap.view"},
    {kind: "SCENARIO", state: "main.scenario.view"},
    {kind: "SERVER", state: "main.server.view"},
    {kind: "DATABASE", state: "main.database.view"},
    {kind: "SOFTWARE", state: "main.software-package.view"},
    {kind: "SOFTWARE_VERSION", state: "main.software-version.view"}, //todo: no separate view for this (for now), just a workaround for the entity-link tooltip
    {kind: "SURVEY_INSTANCE", state: "main.survey.instance.view"},
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
