

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
        .state("main.system.recalculate", recalculateState);
}


setupRoutes.$inject = ["$stateProvider"];


export default setupRoutes;

