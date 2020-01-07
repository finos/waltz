

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

const baseState = {
    url: "system"
};


import SystemAdminList from "./system-admin-list";
import SettingsView from "./settings-view";
import HierarchiesView from "./hierarchies-view";
import OrphansView from "./orphans-view";
import RecalculateView from "./recalculate-view";
import ActorsView from "./actors-view";
import EntityNamedNoteTypesView from "./entity-named-node-types-view";
import InvolvementKindsView from "./involvement-kinds-view";
import StaticPanelsView from "./static-panels-view";
import RatingSchemesView from "./rating-schemes-view";
import EudaListView from "./euda-list-view";

const listViewState = {
    url: "/list",
    views: { "content@": SystemAdminList }
};


const settingsState = {
    url: "/settings",
    views: { "content@": SettingsView }
};


const hierarchiesState = {
    url: "/hierarchies",
    views: { "content@": HierarchiesView }
};


const orphansState = {
    url: "/orphans",
    views: { "content@": OrphansView }
};


const recalculateState = {
    url: "/recalculate",
    views: { "content@": RecalculateView }
};


const actorsState = {
    url: "/actors",
    views: { "content@": ActorsView }
};


const entityNamedNodeTypesState = {
    url: "/entity-named-note-types",
    views: { "content@": EntityNamedNoteTypesView }
};


const involvementKindsState = {
    url: "/involvement-kinds",
    views: { "content@": InvolvementKindsView }
};



const staticPanelsState = {
    url: "/static-panels",
    views: { "content@": StaticPanelsView }
};


const ratingSchemesState = {
    url: "/rating-schemes",
    views: { "content@": RatingSchemesView }
};


const eudaListState = {
    url: "/euda-list",
    views: { "content@": EudaListView }
};


function setupRoutes($stateProvider) {
    $stateProvider
        .state("main.system", baseState)
        .state("main.system.list", listViewState)
        .state("main.system.settings", settingsState)
        .state("main.system.hierarchies", hierarchiesState)
        .state("main.system.orphans", orphansState)
        .state("main.system.actors", actorsState)
        .state("main.system.entity-named-note-types", entityNamedNodeTypesState)
        .state("main.system.involvement-kinds", involvementKindsState)
        .state("main.system.static-panels", staticPanelsState)
        .state("main.system.rating-schemes", ratingSchemesState)
        .state("main.system.euda-list", eudaListState)
        .state("main.system.recalculate", recalculateState);
}


setupRoutes.$inject = ["$stateProvider"];


export default setupRoutes;

